package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.screening.ScreeningFileUtil;
import es.urjc.etsii.grafo.util.*;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/** Execution dispatch and explicit control/data path mapping; Codex and Git editing stay local. */
public final class AutoLlmExecutionService {
    private final AutoLlmProcessRunner processes;
    public AutoLlmExecutionService(AutoLlmProcessRunner processes) { this.processes = processes; }

    public static List<String> validate(AutoLlmConfig config) {
        return validate(config, true);
    }
    private static List<String> validate(AutoLlmConfig config, boolean requireRunner) {
        var errors = new ArrayList<>(AutoLlmConfigUtil.executionErrors(config));
        if (config.execution().workers() != -1 && config.execution().workers() < 1) errors.add("Workers must be -1 (automatic) or a positive integer");
        if (config.execution().target() == AutoLlmConfig.Target.SSH) {
            var pool = config.execution().ssh();
            if (pool == null || pool.servers().isEmpty()) errors.add("SSH execution requires at least one server");
            else {
                var ids = new HashSet<String>();
                for (var ssh : pool.servers()) {
                    try { AutoLlmPoolUtil.requireId(ssh.id()); } catch (IllegalArgumentException e) { errors.add(e.getMessage()); }
                    if (!ids.add(ssh.id())) errors.add("Duplicate target ID: " + ssh.id());
                    if (ssh.host() == null || !ssh.host().matches("[A-Za-z0-9_][A-Za-z0-9_.@:\\[\\]-]*")) errors.add("SSH host must be an alias or user@host");
                    if (ssh.workspace() == null || !ssh.workspace().startsWith("/") || ssh.workspace().equals("/")) errors.add("SSH workspace must be an absolute dedicated directory");
                    if (ssh.javaExecutable().isBlank()) errors.add("Remote Java executable is required");
                    if (ssh.configFile() != null && !Files.isRegularFile(Path.of(ssh.configFile()))) errors.add("SSH configFile does not exist locally");
                    if (ssh.workers() != null && ssh.workers() != -1 && ssh.workers() < 1) errors.add("Target workers must be -1 or positive");
                    if (ssh.heap() != null && !ssh.heap().matches("[1-9][0-9]*[kKmMgG]")) errors.add("Invalid target heap");
                    for (var entry : ssh.environment().entrySet()) if (!entry.getKey().matches("[A-Za-z_][A-Za-z0-9_]*")) errors.add("Invalid remote environment key: " + entry.getKey());
                }
                if (!ids.contains(pool.screeningServerId())) errors.add("Screening server must name a configured server");
                if (requireRunner) {
                    try { if (!Files.isRegularFile(SshTransport.runnerArtifact(config))) errors.add("Remote runner artifact does not exist"); }
                    catch (IllegalArgumentException e) { errors.add(e.getMessage()); }
                }
            }
        }
        return errors;
    }
    public WorkloadProtocol.Host probe(AutoLlmConfig config, Path root) {
        return probe(config, root, null);
    }
    public WorkloadProtocol.Host probe(AutoLlmConfig config, Path root, String selectorPython) {
        var host = inspect(config, root, selectorPython); TargetPreflightUtil.requireReady(host, root); return host;
    }
    public WorkloadProtocol.Host inspect(AutoLlmConfig config, Path root) {
        return inspect(config, root, null);
    }
    public WorkloadProtocol.Host inspect(AutoLlmConfig config, Path root, String selectorPython) {
        return inspect(config, root, selectorPython, true);
    }
    /** Resume checks identity without silently refreshing or discarding qualification evidence. */
    public WorkloadProtocol.Host identity(AutoLlmConfig config, Path root) {
        var host = inspect(config, root, null, !Files.exists(root.resolve("execution-target.json")));
        TargetPreflightUtil.requireReady(host, root);
        return host;
    }
    private WorkloadProtocol.Host inspect(AutoLlmConfig config, Path root, String selectorPython, boolean qualification) {
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) {
            try { return TargetPreflightUtil.inspect(config, root, processes, selectorPython); } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        var errors = validate(config, !Files.exists(root.resolve(".transport/runner.json")));
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var transport = new SshTransport(config, root.resolve(".transport")); transport.bootstrap();
        Path saved = root.resolve("execution-target.json");
        var host = AutoLlmFileUtil.JSON.readValue(transport.call(qualification ? "probe" : "identity", WorkloadPathUtil.remoteRoot(config, root), selectorPython, config), WorkloadProtocol.Host.class);
        if (host.version() != WorkloadProtocol.VERSION) throw new IllegalStateException("Remote runner protocol mismatch");
        if (Files.exists(saved)) {
            var previous = AutoLlmFileUtil.read(saved, WorkloadProtocol.Host.class);
            if (!previous.identity().equals(host.identity()) || previous.effectiveWorkers() != host.effectiveWorkers()) throw new ExecutionTargetException("Execution host or allocation changed; prepare a new campaign");
            if (!qualification) host = new WorkloadProtocol.Host(host.version(), host.identity(), host.requestedWorkers(), host.effectiveWorkers(), host.maxMemoryBytes(), host.totalMemoryBytes(), host.usableDiskBytes(), previous.prerequisites());
        }
        AutoLlmFileUtil.write(root.resolve("target-preflight.json"), host);
        boolean ready = true; for (var check : host.prerequisites()) if (check.status().equals("FAILED")) ready = false;
        if (ready) AutoLlmFileUtil.write(saved, host); return host;
    }
    public int workers(AutoLlmConfig config, Path root) {
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) return ConcurrencyUtil.resolveWorkerCount(config.execution().workers(), Runtime.getRuntime().availableProcessors());
        Path file = root.resolve("execution-target.json");
        return (Files.exists(file) ? AutoLlmFileUtil.read(file, WorkloadProtocol.Host.class) : probe(config, root)).effectiveWorkers();
    }
    public void cancelOutstanding(AutoLlmConfig config, Path directory, Consumer<Map<String,Object>> observer) {
        for (Path descriptor : AutoLlmFileUtil.ownedFiles(directory, "remote-job.json")) {
            if (Files.exists(descriptor.getParent().resolve("collection-completed.json"))) continue;
            var job = AutoLlmFileUtil.read(descriptor, WorkloadProtocol.Job.class);
            Path root = descriptor.getParent();
            for (int i = 0; i < Path.of(job.output()).getNameCount(); i++) root = root.getParent();
            if (root == null || !root.startsWith(directory) || !root.resolve(job.output()).normalize().equals(descriptor.getParent())) throw new ExecutionTargetException("Saved job output does not match its owned directory");
            try { run(config, root, List.of("reattach"), root.resolve(job.cwd()), descriptor.getParent(), null, List.of(), job.timeoutMillis(), job.plannedMillis(), job.watchEvaluations(), job.startupMillis(), () -> true, observer); }
            catch (RuntimeException e) {
                if (Thread.currentThread().isInterrupted() || !Files.exists(descriptor.getParent().resolve("collection-completed.json"))) throw e;
                // Completed or cancelled jobs are reconciled even when their workload failed.
            }
        }
    }
    public AutoLlmConfig stageInputs(AutoLlmConfig config, Path root) {
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) return config;
        try {
            var manifest = new LinkedHashMap<String,Map<String,String>>();
            var paths = new ArrayList<String>();
            for (String input : List.of(config.trainingPath(), config.comparisonPath())) {
                var index = new ArrayList<String>();
                for (String token : InstancePathUtil.list(input)) {
                    Path container = Path.of(IOUtil.containerPath(token));
                    // Keep distinct source paths distinct, even when their names and contents coincide.
                    String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(container.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    Path payload = root.resolve("data/inputs/" + key + "/" + container.getFileName());
                    String suffix = token.contains("!") ? token.substring(token.indexOf('!')) : "";
                    index.add(token);
                    manifest.put(token, Map.of("path", payload + suffix, "sha256", AutoLlmFileUtil.inputHash(token)));
                }
                Path file = root.resolve("control/staged-" + paths.size() + ".index"); Files.createDirectories(file.getParent()); Files.write(file, index); paths.add(file.toString());
            }
            AutoLlmFileUtil.write(root.resolve("data/staged-inputs.json"), manifest);
            return config.withPaths(paths.get(0), paths.get(1));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public AutoLlmProcessRunner.Result run(AutoLlmConfig config, Path root, List<String> command, Path cwd, Path output, Path source,
            List<Path> collect, long timeout, long planned, boolean watch, long startup, BooleanSupplier cancelled, Consumer<Map<String,Object>> observer) {
        if (cancelled.getAsBoolean() && !Files.exists(output.resolve("remote-job.json"))) throw new java.util.concurrent.CancellationException("Cancelled before starting a workload");
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) return new LocalWorkloadExecutor(processes).run(
                new AutoLlmWorkloadExecutor.Workload(command, cwd, output, Map.of(), List.of(), List.of(), timeout, planned, config.execution().watchdogGraceMillis(), watch, startup), cancelled);
        try {
            Path statusFile = output.resolve("remote-status.json"), requestFile = output.resolve("remote-job.json");
            if (Files.exists(statusFile) && Files.exists(requestFile)) {
                String state = AutoLlmFileUtil.JSON.readTree(Files.readString(statusFile)).path("status").asString();
                if (Set.of("CANCELLED", "INTERRUPTED", "FAILED").contains(state) && Files.exists(output.resolve("collection-completed.json"))) {
                    var previous = AutoLlmFileUtil.read(requestFile, WorkloadProtocol.Job.class);
                    String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(root.relativize(output).toString().replace('\\', '/').getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    Path generation = root.resolve(".transfer/generation-" + key + ".json");
                    int next = Files.exists(generation) ? AutoLlmFileUtil.JSON.readTree(Files.readString(generation)).path("value").asInt() + 1 : 1;
                    AutoLlmFileUtil.write(generation, Map.of("value", next));
                    Path archive = root.resolve("remote-attempts/" + previous.id()); Files.createDirectories(archive.getParent());
                    Files.move(output, archive); // retain every byte before starting a fresh execution attempt
                }
            }
            var transport = new SshTransport(config, root.resolve(".transport"));
            var files = new TreeMap<String,Path>(); var reset = new ArrayList<String>();
            if (!Files.exists(output.resolve("remote-job.json"))) {
                for (String directory : List.of("control", "data")) WorkloadArchiveUtil.addTree(files, root, root.resolve(directory), false);
                for (var input : stagedInputs(root).entrySet()) {
                    if (!input.getValue().get("sha256").equals(AutoLlmFileUtil.inputHash(input.getKey()))) throw new IllegalStateException("Staged input changed: " + input.getKey());
                    Path payload = Path.of(IOUtil.containerPath(input.getValue().get("path")));
                    String relative = root.relativize(payload).toString();
                    WorkloadPathUtil.inside(root, relative);
                    // ZIP members retain their original container. No extracted local staging copies are needed.
                    files.put(relative, Path.of(IOUtil.containerPath(input.getKey())));
                }
                // Every stage receives only its declared JAR/winner/elites inputs, not conversations or unrelated local files.
                for (String argument : command) {
                    int equals = argument.indexOf('='); String value = equals < 0 ? argument : argument.substring(equals + 1);
                    if (value.startsWith(root + "/") && Files.isRegularFile(Path.of(value))) files.put(root.relativize(Path.of(value)).toString(), Path.of(value));
                }
                if (source != null) {
                    WorkloadArchiveUtil.addTree(files, root, source, true); reset.add(root.relativize(source).toString());
                    bundleSources(root, source, files);
                }
                materialize(config, root, files);
            }
            var outputs = new ArrayList<String>(); for (Path path : collect) outputs.add(root.relativize(path).toString());
            return new SshWorkloadExecutor(config, root, transport, observer).run(new AutoLlmWorkloadExecutor.Workload(command, cwd, output, files, outputs, reset,
                    timeout, planned, config.execution().watchdogGraceMillis(), watch, startup), cancelled);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void bundleSources(Path root, Path source, Map<String,Path> files) throws IOException {
        var repositories = new ArrayList<Path>(); repositories.add(source);
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) if (!path.equals(source) && Files.exists(path.resolve(".git")) && !path.toString().contains("/.git/")) repositories.add(path);
        }
        for (Path repository : repositories) {
            String relative = root.relativize(repository).toString();
            String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(relative.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            Path metadata = root.resolve(".transfer/head-" + key);
            var head = processes.run(List.of("git", "rev-parse", "HEAD"), repository, metadata, 30000, 0, 1000, () -> false, false, 0);
            if (head.exitCode() != 0) throw new IllegalStateException("Cannot identify source commit: " + repository);
            String commit = Files.readString(metadata.resolve("stdout.log")).strip();
            if (!commit.matches("[a-f0-9]{40,64}")) throw new IllegalStateException("Invalid source commit identity");
            Path bundle = root.resolve(".source-bundles/" + commit + ".bundle"); Files.createDirectories(bundle.getParent());
            if (!Files.exists(bundle)) {
                var result = processes.run(List.of("git", "bundle", "create", bundle.toString(), "HEAD"), repository, root.resolve(".transfer/bundle-" + key), 300000, 0, 1000, () -> false, false, 0);
                if (result.exitCode() != 0) throw new IllegalStateException("Cannot bundle committed source: " + repository);
            }
            Path descriptor = bundle.resolveSibling(key + ".json"); AutoLlmFileUtil.write(descriptor, Map.of("source", relative, "bundle", root.relativize(bundle).toString()));
            files.put(root.relativize(bundle).toString(), bundle); files.put(root.relativize(descriptor).toString(), descriptor);
        }
    }
    private static void materialize(AutoLlmConfig config, Path root, Map<String,Path> files) throws IOException {
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        var staged = new HashMap<String,String>();
        for (var input : stagedInputs(root).entrySet()) staged.put(input.getKey(), input.getValue().get("path"));
        var entries = new ArrayList<>(files.entrySet());
        for (var entry : entries) {
            String name = entry.getKey(); Path target = root.resolve(".transfer/materialized/" + name);
            if (name.equals("control/config.json")) {
                AutoLlmFileUtil.write(target, new AutoLlmConfig(config.project(), config.workspace(), config.trials(), config.seed(), config.maxParameters(),
                        remotePath(config.sotaFile(), root, remote), remotePath(config.initialConfigurationsFile(), root, remote), remotePath(config.trainingPath(), root, remote),
                        remotePath(config.comparisonPath(), root, remote), config.codex(), config.preparation(), config.tuning(), config.comparison(), config.execution()));
                files.put(name, target);
            } else if (name.equals("data/staged-inputs.json")) {
                Map<String,Map<String,String>> manifest = AutoLlmFileUtil.read(entry.getValue(), new TypeReference<>() {});
                var mapped = new LinkedHashMap<String,Map<String,String>>();
                for (var input : manifest.entrySet()) mapped.put(input.getKey(), Map.of("path", remotePath(input.getValue().get("path"), root, remote), "sha256", input.getValue().get("sha256")));
                AutoLlmFileUtil.write(target, mapped); files.put(name, target);
            } else if (name.endsWith(".index")) {
                var tokens = new ArrayList<String>();
                for (String line : Files.readAllLines(entry.getValue())) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String token = InstancePathUtil.resolve(entry.getValue().getParent(), line);
                    tokens.add(remotePath(staged.getOrDefault(token, token), root, remote));
                }
                Files.createDirectories(target.getParent()); Files.write(target, tokens); files.put(name, target);
            } else if (name.equals("control/comparison-time-limits.json")) {
                Map<String,Long> values = AutoLlmFileUtil.read(entry.getValue(), new TypeReference<>() {});
                var mapped = new LinkedHashMap<String,Long>(); for (var value : values.entrySet()) mapped.put(remotePath(staged.getOrDefault(value.getKey(), value.getKey()), root, remote), value.getValue());
                AutoLlmFileUtil.write(target, mapped); files.put(name, target);
            }
        }
    }
    private static String remotePath(String value, Path root, String remote) { return value == null ? null : WorkloadPathUtil.translate(value, root.toString(), remote); }
    private static Map<String,Map<String,String>> stagedInputs(Path root) {
        Path file = root.resolve("data/staged-inputs.json");
        return Files.exists(file) ? AutoLlmFileUtil.read(file, new TypeReference<>() {}) : Map.of();
    }
    public static UnaryOperator<String> localPaths(AutoLlmConfig config, Path root) {
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) return UnaryOperator.identity();
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        var originals = new HashMap<String,String>();
        for (var input : stagedInputs(root).entrySet()) originals.put(input.getValue().get("path"), input.getKey());
        return path -> {
            String local = WorkloadPathUtil.translate(path, remote, root.toString());
            return originals.getOrDefault(local, local);
        };
    }
    public static AutoLlmCheckOrchestrator.Check check(AutoLlmConfig config, Path root, Path file) {
        var check = AutoLlmFileUtil.read(file, AutoLlmCheckOrchestrator.Check.class);
        var paths = localPaths(config, root);
        var training = new ArrayList<String>(); var comparison = new ArrayList<String>();
        for (String path : check.training()) training.add(paths.apply(path));
        for (String path : check.comparison()) comparison.add(paths.apply(path));
        return new AutoLlmCheckOrchestrator.Check(check.parameters(), check.added(), training, comparison);
    }
}
