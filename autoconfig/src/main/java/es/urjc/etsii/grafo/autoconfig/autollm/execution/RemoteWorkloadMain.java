package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.screening.ScreeningFileUtil;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.springframework.boot.system.ApplicationHome;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CancellationException;
import static es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.*;

/** SSH entry point. No Spring context, dashboard, model classes or credentials are required. */
public final class RemoteWorkloadMain {
    private RemoteWorkloadMain() {}
    public static void main(String[] args) throws Exception {
        try { execute(args); }
        catch (Exception e) { e.printStackTrace(System.err); System.exit(2); }
    }
    private static void execute(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Expected action and absolute workspace");
        Path root = Path.of(args[1]).toAbsolutePath().normalize();
        if (!Path.of(args[1]).isAbsolute()) throw new IllegalArgumentException("Workspace must be absolute");
        Files.createDirectories(root);
        String id = args.length > 2 ? args[2] : "";
        switch (args[0]) {
            case "identify" -> print(Map.of("version", VERSION, "sha256", AutoLlmFileUtil.hash(Path.of(System.getProperty("java.class.path")))));
            case "identity" -> {
                var config = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), AutoLlmConfig.class);
                print(TargetPreflightUtil.identity(config, root, List.of()));
            }
            case "has" -> {
                Path file = WorkloadPathUtil.inside(root, id);
                print(Map.of("sha256", Files.isRegularFile(file) ? AutoLlmFileUtil.hash(file) : ""));
            }
            case "probe" -> {
                try (var channel = FileChannel.open(WorkloadPathUtil.workspace(root).resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
                if (lock == null) throw new IllegalStateException("Another remote workload owns this workspace; wait before preflight");
                var config = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), AutoLlmConfig.class);
                print(TargetPreflightUtil.inspect(config, root, new AutoLlmProcessRunner(), args.length > 2 ? id : null));
                }
            }
            case "put" -> {
                Path target = WorkloadPathUtil.inside(root, id);
                Files.createDirectories(target.getParent());
                Path temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".tmp");
                try (var output = Files.newOutputStream(temporary)) { System.in.transferTo(output); }
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); print(Map.of("sha256", AutoLlmFileUtil.hash(target)));
            }
            case "submit" -> submit(root, id);
            case "supervise" -> supervise(root, id);
            case "status" -> print(state(root, id));
            case "poll" -> poll(root, id);
            case "cancel" -> { Files.writeString(jobDirectory(root, id).resolve("cancel"), Instant.now().toString()); print(state(root, id)); }
            case "collect" -> {
                State state = state(root, id);
                if (state.archiveSha256() == null) throw new IllegalStateException("Job evidence is not complete");
                Files.copy(jobDirectory(root, id).resolve("result.zip"), System.out);
            }
            default -> throw new IllegalArgumentException("Unknown workload action: " + args[0]);
        }
    }
    private static void print(Object value) throws IOException { System.out.write(AutoLlmFileUtil.JSON.writeValueAsBytes(value)); }
    private static Path jobDirectory(Path root, String id) throws IOException {
        if (!id.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid workload ID");
        Path directory = root.resolve(".jobs/" + id); Files.createDirectories(directory); return directory;
    }
    private static void submit(Path root, String id) throws Exception {
        Path directory = jobDirectory(root, id);
        Job requested = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), Job.class);
        if (requested.version() != VERSION || !id.equals(requested.id()) || !root.toString().equals(requested.root())) throw new IllegalArgumentException("Workload protocol, root or identity mismatch");
        WorkloadPathUtil.inside(root, requested.cwd()); WorkloadPathUtil.inside(root, requested.output());
        for (String path : requested.collect()) WorkloadPathUtil.inside(root, path);
        for (String path : requested.layers()) if (!path.matches("\\.layers/[a-f0-9]{64}\\.zip")) throw new IllegalArgumentException("Invalid input layer");
        for (String path : requested.reset()) {
            if (path.isBlank() || path.equals(".")) throw new IllegalArgumentException("Cannot reset workspace root");
            WorkloadPathUtil.inside(root, path);
        }
        try (var channel = FileChannel.open(directory.resolve("submit.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
            Path file = directory.resolve("job.json");
            if (Files.exists(file)) {
                if (!requested.equals(AutoLlmFileUtil.read(file, Job.class))) throw new IllegalStateException("Workload ID already has a different payload");
            } else {
                if (!requested.inputSha256().equals(AutoLlmFileUtil.hash(directory.resolve("input.zip")))) throw new IllegalStateException("Workload input checksum mismatch");
                AutoLlmFileUtil.write(file, requested);
                AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, JobStatus.SUBMITTED, null, null, null));
            }
            State current = state(root, id);
            if (current.status() == JobStatus.SUBMITTED && !Files.exists(directory.resolve("started.json"))) {
                var command = new ArrayList<>(List.of("nohup")); command.addAll(selfCommand()); command.addAll(List.of("supervise", root.toString(), id));
                new ProcessBuilder(command).directory(root.toFile()).redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")))
                        .redirectOutput(directory.resolve("supervisor.log").toFile()).redirectErrorStream(true).start();
            }
        }
        print(state(root, id));
    }
    private static List<String> selfCommand() {
        var result = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString()));
        File source = new ApplicationHome(RemoteWorkloadMain.class).getSource();
        if (source != null && source.isFile()) result.addAll(List.of("-jar", source.toString()));
        else result.addAll(List.of("-cp", System.getProperty("java.class.path"), RemoteWorkloadMain.class.getName()));
        return result;
    }
    private static void supervise(Path root, String id) throws Exception {
        System.setProperty("mork.execution.remote", "true");
        Path directory = jobDirectory(root, id);
        try (var channel = FileChannel.open(directory.resolve("run.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
            if (lock == null || Files.exists(directory.resolve("started.json"))) return;
            AutoLlmFileUtil.write(directory.resolve("started.json"), Map.of("pid", ProcessHandle.current().pid(), "start", ProcessHandle.current().info().startInstant().orElseThrow().toString()));
            Job job = AutoLlmFileUtil.read(directory.resolve("job.json"), Job.class);
            AutoLlmProcessRunner.Result result = null; String failure = null; JobStatus outcome = JobStatus.FAILED;
            try (var workspace = FileChannel.open(WorkloadPathUtil.workspace(root).resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var ownership = workspace.tryLock()) {
                if (ownership == null) throw new IllegalStateException("Another remote measurement owns this workspace");
                if (!job.expectedHost().isEmpty() && !job.expectedHost().equals(ScreeningFileUtil.host())) throw new ExecutionTargetException("Execution host or allocation changed; prepare and review a new dataset");
                if (!job.inputSha256().equals(AutoLlmFileUtil.hash(directory.resolve("input.zip")))) throw new IllegalStateException("Frozen job input changed");
                for (String path : job.reset()) WorkloadArchiveUtil.deleteTree(WorkloadPathUtil.inside(root, path));
                for (String layer : job.layers()) {
                    Path archive = WorkloadPathUtil.inside(root, layer);
                    WorkloadArchiveUtil.installLayer(archive, root);
                }
                WorkloadArchiveUtil.unpack(directory.resolve("input.zip"), root);
                restoreGit(root);
                AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, JobStatus.RUNNING, null, null, null));
                var command = new ArrayList<String>();
                if (!job.environment().isEmpty()) {
                    command.add("env");
                    for (var entry : job.environment().entrySet()) command.add(entry.getKey() + "=" + entry.getValue());
                }
                command.addAll(job.command());
                String executable = job.command().getFirst();
                if (executable.endsWith("/mvnw") || executable.endsWith("/gradlew")) {
                    var wrapper = new ArrayList<>(command.subList(0, command.size() - job.command().size()));
                    wrapper.add(executable); wrapper.add("--version");
                    Path evidence = WorkloadPathUtil.inside(root, job.output()).resolve("wrapper-preflight");
                    var version = new AutoLlmProcessRunner().run(wrapper, WorkloadPathUtil.inside(root, job.cwd()), evidence, job.timeoutMillis(), 0, job.graceMillis(), () -> Files.exists(directory.resolve("cancel")), false, 0);
                    if (version.exitCode() != 0) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "BUILD_WRAPPER", "Committed build wrapper did not run; inspect its version diagnostics", evidence);
                }
                result = new AutoLlmProcessRunner().run(command, WorkloadPathUtil.inside(root, job.cwd()), WorkloadPathUtil.inside(root, job.output()),
                        job.timeoutMillis(), job.plannedMillis(), job.graceMillis(), () -> Files.exists(directory.resolve("cancel")), job.watchEvaluations(), job.startupMillis());
                outcome = result.exitCode() == 0 || result.plannedDeadline() ? JobStatus.SUCCEEDED : JobStatus.FAILED;
            } catch (CancellationException e) { outcome = JobStatus.CANCELLED; failure = e.toString(); }
            catch (ExecutionTargetException e) { outcome = JobStatus.HOST_CHANGED; failure = e.toString(); }
            catch (Exception e) {
                failure = e.toString();
                if (e instanceof RuntimeException problem) AutoLlmFileUtil.write(WorkloadPathUtil.inside(root, job.output()).resolve("execution/failure.json"), AutoLlmFailureUtil.describe(problem, WorkloadPathUtil.inside(root, job.output())));
            }
            State completed = new State(id, outcome, result, failure, null);
            AutoLlmFileUtil.write(directory.resolve("execution-completed.json"), completed);
            AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, JobStatus.COLLECTING, result, failure, null));
            collectResult(root, id, job, completed);
        }
    }
    /** Retry collection independently if the supervisor exits after execution finishes. */
    private static void collectResult(Path root, String id, Job job, State completed) throws IOException {
        Path directory = jobDirectory(root, id);
            var files = new TreeMap<String,Path>();
            DirectoryArchiveUtil.archive(WorkloadPathUtil.inside(root, job.output()).resolve("solutions"));
            WorkloadArchiveUtil.addTree(files, root, WorkloadPathUtil.inside(root, job.output()), false);
            for (String path : job.collect()) WorkloadArchiveUtil.addTree(files, root, WorkloadPathUtil.inside(root, path), false);
            if (!job.reset().isEmpty()) try (var paths = Files.walk(WorkloadPathUtil.inside(root, job.cwd()))) {
                for (Path path : paths.toList()) if (Files.isDirectory(path) && path.getFileName().toString().equals("surefire-reports")) WorkloadArchiveUtil.addTree(files, root, path, false);
            }
            markRemoteOwners(files);
            WorkloadArchiveUtil.pack(directory.resolve("result.zip"), files);
            AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, completed.status(), completed.result(), completed.failure(), AutoLlmFileUtil.hash(directory.resolve("result.zip"))));
    }
    private static void markRemoteOwners(Map<String,Path> files) {
        for (var entry : files.entrySet()) if (entry.getKey().endsWith("/owner.json")) {
            Map<String,Object> record = AutoLlmFileUtil.read(entry.getValue(), new TypeReference<>() {});
            record.put("executionTarget", "SSH"); AutoLlmFileUtil.write(entry.getValue(), record);
        }
    }
    private static void restoreGit(Path root) throws Exception {
        Path descriptors = root.resolve(".source-bundles");
        if (!Files.isDirectory(descriptors)) return;
        try (var paths = Files.list(descriptors)) {
            for (Path descriptor : paths.toList()) if (descriptor.toString().endsWith(".json")) {
                Map<String,String> entry = AutoLlmFileUtil.read(descriptor, new TypeReference<>() {});
                Path source = WorkloadPathUtil.inside(root, entry.get("source"));
                if (Files.exists(source.resolve(".git"))) continue;
                for (List<String> command : List.of(List.of("git", "init", "--quiet"), List.of("git", "fetch", "--quiet", WorkloadPathUtil.inside(root, entry.get("bundle")).toString(), "HEAD"), List.of("git", "reset", "--mixed", "--quiet", "FETCH_HEAD"))) {
                    var process = new ProcessBuilder(command).directory(source.toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                    if (!process.waitFor(30000, java.util.concurrent.TimeUnit.MILLISECONDS)) { process.destroyForcibly(); throw new IllegalStateException("Git snapshot restore timed out"); }
                    if (process.exitValue() != 0) throw new IllegalStateException("Cannot restore source Git metadata");
                }
            }
        }
    }
    private static State state(Path root, String id) throws IOException {
        Path directory = jobDirectory(root, id), file = directory.resolve("state.json");
        if (!Files.exists(file)) return new State(id, JobStatus.NOT_FOUND, null, null, null);
        State state = AutoLlmFileUtil.read(file, State.class);
        Path owner = directory.resolve("started.json");
        if (state.status().inProgress() && Files.exists(owner)) {
            var identity = AutoLlmFileUtil.read(owner, Map.class);
            boolean alive = ProcessHandle.of(((Number) identity.get("pid")).longValue()).filter(ProcessHandle::isAlive)
                    .flatMap(p -> p.info().startInstant()).map(Instant::toString).orElse("").equals(identity.get("start"));
            if (!alive) {
                try (var channel = FileChannel.open(directory.resolve("run.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
                if (lock == null) return AutoLlmFileUtil.read(file, State.class);
                state = AutoLlmFileUtil.read(file, State.class);
                if (!state.status().inProgress()) return state;
                System.setProperty("mork.execution.remote", "true");
                Job job = AutoLlmFileUtil.read(directory.resolve("job.json"), Job.class);
                Path completed = directory.resolve("execution-completed.json");
                if (Files.isRegularFile(completed)) {
                    collectResult(root, id, job, AutoLlmFileUtil.read(completed, State.class));
                    return AutoLlmFileUtil.read(file, State.class);
                }
                new AutoLlmProcessRunner().reconcile(WorkloadPathUtil.inside(root, job.output()));
                var files = new TreeMap<String,Path>(); WorkloadArchiveUtil.addTree(files, root, WorkloadPathUtil.inside(root, job.output()), false);
                WorkloadArchiveUtil.pack(directory.resolve("result.zip"), files);
                state = new State(id, JobStatus.INTERRUPTED, null, "Remote supervisor exited before completion", AutoLlmFileUtil.hash(directory.resolve("result.zip")));
                AutoLlmFileUtil.write(file, state);
                }
            }
        }
        return state;
    }
    private static void poll(Path root, String id) throws IOException {
        Path directory = jobDirectory(root, id);
        State state = state(root, id);
        var chunks = new LinkedHashMap<String,Chunk>(); var snapshots = new LinkedHashMap<String,String>();
        Map<String,Long> offsets = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), new TypeReference<>() {});
        if (Files.exists(directory.resolve("job.json"))) {
            Job job = AutoLlmFileUtil.read(directory.resolve("job.json"), Job.class); Path output = WorkloadPathUtil.inside(root, job.output());
            for (String name : List.of("stdout.log", "stderr.log", "execution/events.jsonl")) {
                Path file = output.resolve(name);
                if (!Files.isRegularFile(file)) continue;
                long offset = offsets.getOrDefault(name, 0L);
                if (offset < 0 || offset > Files.size(file)) throw new IllegalArgumentException("Invalid log cursor");
                try (var input = new RandomAccessFile(file.toFile(), "r")) {
                    input.seek(offset); byte[] data = new byte[(int) Math.min(65536, input.length() - offset)]; input.readFully(data);
                    chunks.put(name, new Chunk(offset, Base64.getEncoder().encodeToString(data)));
                }
            }
            for (String name : List.of("execution/tuning-progress.json", "execution/tuning-status.json", "execution/probes.json", "execution/readiness.json")) {
                Path file = output.resolve(name);
                if (Files.isRegularFile(file) && Files.size(file) < 4_000_000) snapshots.put(root.relativize(file).toString(), Files.readString(file));
            }
            for (String collected : job.collect()) {
                Path file = WorkloadPathUtil.inside(root, collected).resolve("screening-progress.json");
                if (Files.isRegularFile(file) && Files.size(file) < 4_000_000) snapshots.put(root.relativize(file).toString(), Files.readString(file));
            }
        }
        print(new Poll(state, chunks, snapshots));
    }
}
