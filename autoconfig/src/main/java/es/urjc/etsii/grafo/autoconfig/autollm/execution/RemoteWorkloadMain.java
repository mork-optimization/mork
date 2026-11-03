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
    public record DeleteRequest(OwnershipManifest.Resource resource, Set<String> references) {}
    private RemoteWorkloadMain() {}
    public static void main(String[] args) throws Exception {
        try { execute(args); }
        catch (Exception e) { e.printStackTrace(System.err); System.exit(2); }
    }
    private static void execute(String[] args) throws Exception {
        if (args.length == 3 && args[0].equals("repair-collection")) {
            print(RemoteCollectionRecovery.repair(Path.of(args[1]), Path.of(args[2]))); return;
        }
        if (args.length < 2) throw new IllegalArgumentException("Expected action and absolute workspace");
        Path root = AutoLlmNormalization.localPath(Path.of(args[1]));
        if (!Path.of(args[1]).isAbsolute()) throw new IllegalArgumentException("Workspace must be absolute");
        if (!Set.of("inspect", "storage", "cleanup-owned", "cleanup-ack", "cleanup-progress", "ownership-plan", "ownership-release").contains(args[0])) Files.createDirectories(root);
        String id = args.length > 2 ? args[2] : "";
        switch (args[0]) {
            case "identify" -> print(Map.of("version", VERSION, "sha256", AutoLlmFileUtil.hash(Path.of(System.getProperty("java.class.path"))), "features", List.of("CAMPAIGN_ASSETS", "COLLECTION_ACK", "STORAGE_OBSERVATION")));
            case "cleanup-finish" -> AutoLlmCleanup.finish(Path.of(id));
            case "ownership-register" -> {
                var request = AutoLlmFileUtil.JSON.readTree(System.in.readAllBytes());
                new OwnershipManifest(WorkloadPathUtil.workspace(root)).directory(WorkloadPathUtil.remoteCampaign(root), "remote-campaign", request.path("reference").asString());
                print(Map.of("resource", new OwnershipManifest(WorkloadPathUtil.workspace(root)).directory(root, "execution", request.path("reference").asString()), "workspaceId", WorkspaceState.identity(WorkloadPathUtil.workspace(root))));
            }
            case "ownership-release" -> { new OwnershipManifest(WorkloadPathUtil.workspace(root)).removeReferences(AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), new TypeReference<Set<String>>() {})); print(Map.of("accepted", true)); }
            case "ownership-plan" -> {
                var resources = new ArrayList<OwnershipManifest.Resource>();
                for (var resource : new OwnershipManifest(WorkloadPathUtil.workspace(root)).resources()) if (resource.path().equals(root.toString()) || resource.path().equals(WorkloadPathUtil.remoteCampaign(root).toString())) resources.add(resource);
                print(new ExecutionOwner.Inventory(WorkspaceState.identity(WorkloadPathUtil.workspace(root)), resources));
            }
            case "cleanup-progress" -> print(Map.of("phase", new OwnershipManifest(WorkloadPathUtil.workspace(root)).deletionPhase(AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), OwnershipManifest.Resource.class))));
            case "cleanup-ack" -> { new OwnershipManifest(WorkloadPathUtil.workspace(root)).acknowledgeDeletion(AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), OwnershipManifest.Resource.class)); print(Map.of("accepted", true)); }
            case "cleanup-owned" -> {
                var request = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), DeleteRequest.class);
                if (!request.resource().path().equals(root.toString())) throw new IllegalArgumentException("Cleanup root mismatch");
                try (var lock = WorkspaceState.acquire(WorkloadPathUtil.workspace(root), () -> false, () -> {})) {
                    WorkspaceState.reconcileActivity(WorkloadPathUtil.workspace(root));
                    new OwnershipManifest(WorkloadPathUtil.workspace(root)).delete(request.resource(), request.references());
                }
                print(Map.of("deleted", true));
            }
            case "job-collected" -> {
                Path collected = jobDirectory(root, id).resolve("collected.json");
                if (Files.isRegularFile(collected)) { print(Map.of("accepted", true)); break; }
                Job job = AutoLlmFileUtil.read(jobDirectory(root, id).resolve("job.json"), Job.class);
                if (state(root, id).status().inProgress()) throw new IllegalStateException("Job still active");
                AutoLlmProcessRunner.requireTerminated(WorkloadPathUtil.inside(root, job.output()));

                AutoLlmFileUtil.write(collected, Map.of("jobId", id));
                print(Map.of("accepted", true));
            }
            case "identity" -> {
                var config = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), AutoLlmConfig.class);
                print(TargetPreflightUtil.identity(config, root, List.of()));
            }
            case "has" -> {
                Path file = RemoteCampaignStorage.shared(id) ? RemoteCampaignStorage.layer(root, id) : WorkloadPathUtil.inside(root, id);
                print(Map.of("sha256", Files.isRegularFile(file) ? AutoLlmFileUtil.hash(file) : ""));
            }
            case "storage" -> {
                Path existing = root;
                while (existing != null && !Files.exists(existing)) existing = existing.getParent();
                if (existing == null) throw new IOException("No filesystem available for " + root);
                print(Map.of("usableDiskBytes", Files.getFileStore(existing).getUsableSpace(), "observedAt", Instant.now().toString()));
            }
            case "retain-layers" -> { RemoteCampaignStorage.retainLayers(root, AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), new TypeReference<List<String>>() {})); print(Map.of("retained", true)); }
            case "acknowledge" -> print(RemoteCampaignStorage.acknowledge(root, id, AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), RemoteCampaignStorage.Acknowledgement.class)));
            case "retire" -> print(RemoteCampaignStorage.retire(root));
            case "probe" -> {
                var config = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), AutoLlmConfig.class);
                try (var lock = WorkspaceState.execute(WorkloadPathUtil.workspace(root), root, () -> AutoLlmCleanup.requested(root), () -> {})) {
                    print(TargetPreflightUtil.inspect(config, root, new AutoLlmProcessRunner(), args.length > 2 ? id : null));
                }
            }
            case "put" -> {
                Path target = RemoteCampaignStorage.shared(id) ? RemoteCampaignStorage.layer(root, id) : WorkloadPathUtil.inside(root, id);
                Files.createDirectories(target.getParent());
                Path temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".tmp");
                try {
                    try (var output = Files.newOutputStream(temporary)) { System.in.transferTo(output); }
                    String hash = AutoLlmFileUtil.hash(temporary);
                    if (RemoteCampaignStorage.shared(id) && !target.getFileName().toString().equals(hash + ".zip")) throw new IOException("Shared layer upload checksum mismatch");
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); print(Map.of("sha256", hash));
                } finally { Files.deleteIfExists(temporary); }
            }
            case "submit" -> submit(root, id);
            case "supervise" -> supervise(root, id);
            case "inspect" -> print(inspect(root, id));
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
    /** Observational counterpart to status: never reconciles jobs or collects evidence. */
    private static Object inspect(Path root, String id) throws IOException {
        if (!id.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid workload ID");
        Path directory = root.resolve(".jobs/" + id);
        var result = new LinkedHashMap<String,Object>();
        result.put("jobId", id); result.put("checkedAt", Instant.now().toString());
        for (String name : List.of("state.json", "execution-completed.json", "collection-metrics.json"))
            if (Files.isRegularFile(directory.resolve(name))) result.put(name, AutoLlmFileUtil.read(directory.resolve(name), Object.class));
        Path started = directory.resolve("started.json");
        if (Files.isRegularFile(started)) {
            Map<String,Object> identity = AutoLlmFileUtil.read(started, new TypeReference<>() {});
            boolean alive = ProcessHandle.of(((Number) identity.get("pid")).longValue()).filter(ProcessHandle::isAlive)
                    .flatMap(p -> p.info().startInstant()).map(Instant::toString).orElse("").equals(identity.get("start"));
            result.put("supervisorAlive", alive);
        }
        Path descriptor = directory.resolve("job.json");
        if (Files.isRegularFile(descriptor)) {
            Job job = AutoLlmFileUtil.read(descriptor, Job.class);
            result.put("identityMatches", job.id().equals(id) && job.root().equals(root.toString()));
            result.put("hostMatches", job.expectedHost().isEmpty() || job.expectedHost().equals(TargetPreflightUtil.hostIdentity()));
            for (String name : List.of("owner.json", "execution/terminal.json", "execution/tuning-progress.json")) {
                Path file = WorkloadPathUtil.inside(root, job.output()).resolve(name);
                if (Files.isRegularFile(file)) {
                    result.put(name, AutoLlmFileUtil.read(file, Object.class));
                    if (name.equals("owner.json")) {
                        Map<String,Object> owner = AutoLlmFileUtil.read(file, new TypeReference<>() {});
                        boolean alive = ProcessHandle.of(((Number) owner.get("pid")).longValue()).filter(ProcessHandle::isAlive)
                                .flatMap(p -> p.info().startInstant()).map(Instant::toString).orElse("").equals(owner.get("start"));
                        result.put("childAlive", alive);
                    }
                    result.put(name + "UpdatedAt", Files.getLastModifiedTime(file).toInstant().toString());
                }
            }
        }
        result.put("found", Files.isRegularFile(descriptor));
        return result;
    }
    private static void print(Object value) throws IOException { System.out.write(AutoLlmFileUtil.JSON.writeValueAsBytes(value)); }
    private static Path jobDirectory(Path root, String id) throws IOException {
        if (!id.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid workload ID");
        Path directory = root.resolve(".jobs/" + id); Files.createDirectories(directory); return directory;
    }
    private static void submit(Path root, String id) throws Exception {
        Path directory = jobDirectory(root, id);
        try (var owner = FileChannel.open(root.resolve(".storage-owner.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var ownerLock = owner.lock()) {
        Job requested = AutoLlmFileUtil.JSON.readValue(System.in.readAllBytes(), Job.class);
        if (requested.version() != VERSION || !id.equals(requested.id()) || !root.toString().equals(requested.root())) throw new IllegalArgumentException("Workload protocol, root or identity mismatch");
        if (Files.exists(root.resolve("storage-retired.json"))) {
            Path saved = directory.resolve("job.json");
            if (!Files.isRegularFile(saved) || !requested.equals(AutoLlmFileUtil.read(saved, Job.class))) throw new IllegalStateException("Cannot submit new work to a retired owner");
            print(state(root, id)); return;
        }
        ExecutionOwner.of(requested);
        WorkloadPathUtil.inside(root, requested.cwd()); WorkloadPathUtil.inside(root, requested.output());
        for (String path : requested.collect()) WorkloadPathUtil.inside(root, path);
        for (String path : requested.layers()) if (!(requested.managedStorage() ? RemoteCampaignStorage.shared(path) : path.matches("\\.layers/[a-f0-9]{64}\\.zip"))) throw new IllegalArgumentException("Invalid input layer");
        if (requested.managedStorage()) RemoteCampaignStorage.retainLayers(root, requested.layers());
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
                Path intent = directory.resolve("spawn-intent.json"), spawned = directory.resolve("spawned.json");
                if (Files.exists(intent)) {
                    if (!Files.isRegularFile(spawned) || !AutoLlmFileUtil.read(spawned, ProcessIdentity.class).alive())
                        throw new IllegalStateException("Submission outcome uncertain; reconcile saved process identity before retrying");
                } else {
                    AutoLlmFileUtil.write(intent, Map.of("id", id));
                    var command = new ArrayList<>(selfCommand()); command.addAll(List.of("supervise", root.toString(), id));
                    var process = new ProcessBuilder(command).directory(root.toFile()).redirectOutput(directory.resolve("supervisor.log").toFile()).redirectErrorStream(true).start();
                    process.getOutputStream().close();
                    AutoLlmFileUtil.write(spawned, ProcessIdentity.of(process.toHandle()));
                }
            }
        }
        print(state(root, id));
        }
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
            try (var workspaceLock = WorkspaceState.execute(WorkloadPathUtil.workspace(root), root, () -> Files.exists(directory.resolve("cancel")),
                    () -> AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, JobStatus.WAITING_FOR_WORKSPACE, null, "Workspace is busy", null)))) {
                ExecutionOwner.of(job);
                if (!job.expectedHost().isEmpty() && !job.expectedHost().equals(TargetPreflightUtil.hostIdentity())) throw new ExecutionTargetException("Execution host or allocation changed; prepare and review a new dataset");
                if (!job.inputSha256().equals(AutoLlmFileUtil.hash(directory.resolve("input.zip")))) throw new IllegalStateException("Frozen job input changed");
                for (String path : job.reset()) WorkloadArchiveUtil.deleteTree(WorkloadPathUtil.inside(root, path));
                for (String layer : job.layers()) {
                    RemoteCampaignStorage.install(root, layer);
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
        long collectionStarted = System.nanoTime();
        Path directory = jobDirectory(root, id);
            var files = new TreeMap<String,Path>();
            DirectoryArchiveUtil.archive(WorkloadPathUtil.inside(root, job.output()).resolve("solutions"));
            WorkloadArchiveUtil.addTree(files, root, WorkloadPathUtil.inside(root, job.output()), false);
            for (String path : job.collect()) WorkloadArchiveUtil.addTree(files, root, WorkloadPathUtil.inside(root, path), false);
            // A queued job can be cancelled before input installation creates cwd.
            if (!job.reset().isEmpty() && (completed.status() == JobStatus.SUCCEEDED || Files.isDirectory(WorkloadPathUtil.inside(root, job.cwd())))) try (var paths = Files.walk(WorkloadPathUtil.inside(root, job.cwd()))) {
                for (Path path : paths.toList()) if (Files.isDirectory(path) && path.getFileName().toString().equals("surefire-reports")) WorkloadArchiveUtil.addTree(files, root, path, false);
            }
            markRemoteOwners(files);
            WorkloadArchiveUtil.pack(directory.resolve("result.zip"), files);
            AutoLlmFileUtil.write(directory.resolve("collection-metrics.json"), Map.of("archiveBytes", Files.size(directory.resolve("result.zip")), "collectionMillis", (System.nanoTime() - collectionStarted) / 1_000_000));
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
                for (List<String> command : List.of(List.of("git", "init", "--quiet"), List.of("git", "fetch", "--quiet", WorkloadPathUtil.inside(root, entry.get("bundle")).toString(), entry.getOrDefault("ref", "HEAD")), List.of("git", "reset", "--mixed", "--quiet", "FETCH_HEAD"))) {
                    Path evidence = root.resolve(".git-restore/" + AutoLlmFileUtil.canonicalHash(List.of(source.toString(), command)));
                    var result = new AutoLlmProcessRunner().run(command, source, evidence, 30000, 0, 1000, () -> AutoLlmCleanup.requested(root), false, 0);
                    if (result.exitCode() != 0) throw new IllegalStateException("Cannot restore source Git metadata; inspect " + evidence);
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
                Path output = WorkloadPathUtil.inside(root, job.output());
                AutoLlmProcessRunner.requireTerminated(root);
                new AutoLlmProcessRunner().reconcile(output);
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
