package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.*;

public final class SshWorkloadExecutor implements AutoLlmWorkloadExecutor {
    private final AutoLlmConfig config;
    private final Path root;
    private final SshTransport transport;
    private final Consumer<Map<String,Object>> observer;
    private String lastSynchronizedAt;
    private int connectionRetries;
    public SshWorkloadExecutor(AutoLlmConfig config, Path root, SshTransport transport, Consumer<Map<String,Object>> observer) {
        this.config = config; this.root = root; this.transport = transport; this.observer = observer;
    }
    @Override public AutoLlmProcessRunner.Result run(Workload work, BooleanSupplier cancelled) {
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        Path descriptor = work.output().resolve("remote-job.json");
        Job job;
        try {
            Files.createDirectories(work.output());
            if (Files.exists(descriptor)) job = AutoLlmFileUtil.read(descriptor, Job.class);
            else {
                String name = root.relativize(work.output()).toString().replace('\\', '/');
                String key = AutoLlmFileUtil.hash(new ByteArrayInputStream(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                Path generationFile = root.resolve(".transfer/generation-" + key + ".json");
                int generation = Files.exists(generationFile) ? AutoLlmFileUtil.JSON.readTree(Files.readString(generationFile)).path("value").asInt() : 0;
                String id = generation == 0 ? key : AutoLlmFileUtil.hash(new ByteArrayInputStream((name + ":" + generation).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                var immutable = new TreeMap<String,Path>(); var mutable = new TreeMap<String,Path>();
                for (var entry : work.inputs().entrySet()) {
                    if (entry.getKey().startsWith("data/") || entry.getKey().startsWith(".source-bundles/") && entry.getKey().endsWith(".bundle")) immutable.put(entry.getKey(), entry.getValue());
                    else mutable.put(entry.getKey(), entry.getValue());
                }
                var layers = new ArrayList<String>();
                if (!immutable.isEmpty()) {
                    Path layer = root.resolve(".transfer/layer-" + id + ".zip"); WorkloadArchiveUtil.pack(layer, immutable);
                    String hash = AutoLlmFileUtil.hash(layer); Path saved = root.resolve(".transfer/layers/" + hash + ".zip"); Files.createDirectories(saved.getParent());
                    if (!Files.exists(saved)) Files.move(layer, saved); else Files.delete(layer);
                    layers.add(".layers/" + hash + ".zip");
                }
                Path archive = root.resolve(".transfer/" + id + ".zip"); WorkloadArchiveUtil.pack(archive, mutable);
                var command = new ArrayList<String>();
                for (String argument : work.command()) command.add(WorkloadPathUtil.argument(argument, root, remote));
                if (work.command().getFirst().equals(Path.of(System.getProperty("java.home"), "bin/java").toString()) || work.command().getFirst().equals("java")) {
                    command.set(0, config.execution().ssh().javaExecutable()); command.add(1, "-Dmork.execution.remote=true");
                }
                var reset = new ArrayList<>(work.reset()); reset.add(name);
                job = new Job(VERSION, id, remote, AutoLlmFileUtil.hash(archive), layers, command, root.relativize(work.cwd()).toString(), name, work.collect(), reset,
                        config.execution().ssh().environment(), Files.exists(root.resolve("execution-target.json")) ? AutoLlmFileUtil.read(root.resolve("execution-target.json"), Host.class).identity() : Map.of(),
                        work.timeoutMillis(), work.plannedMillis(), work.graceMillis(), work.watchEvaluations(), work.startupMillis());
                AutoLlmFileUtil.write(descriptor, job); // durable identity before any uncertain remote submission
            }
            if (!remote.equals(job.root())) throw new ExecutionTargetException("Remote workspace changed; reconcile the original job before configuring another target");
            Path previous = work.output().resolve("remote-status.json");
            if (Files.exists(previous)) {
                var saved = AutoLlmFileUtil.JSON.readTree(Files.readString(previous));
                lastSynchronizedAt = saved.path("lastSynchronizedAt").asString(null); connectionRetries = saved.path("connectionRetries").asInt(0);
            }
            var offsets = new HashMap<String,Long>();
            for (String name : List.of("stdout.log", "stderr.log", "execution/events.jsonl")) {
                Path file = work.output().resolve(name); offsets.put(name, Files.exists(file) ? Files.size(file) : 0L);
            }
            boolean cancellationSent = false; boolean initialized = false; boolean verified = false;
            int connectionFailures = 0;
            while (true) {
                if (Thread.currentThread().isInterrupted()) throw new CancellationException("Remote job detached; resume the saved job identity");
                try {
                    if (!initialized) { transport.bootstrap(); initialized = true; }
                    State state = AutoLlmFileUtil.JSON.readValue(transport.call("status", remote, job.id(), Map.of()), State.class);
                    if (state.status().equals("NOT_FOUND")) {
                        for (String layer : job.layers()) {
                            String hash = Path.of(layer).getFileName().toString().replace(".zip", "");
                            var cached = AutoLlmFileUtil.JSON.readTree(transport.call("has", remote, layer, Map.of()));
                            if (!hash.equals(cached.path("sha256").asString())) transport.upload(remote, layer, root.resolve(".transfer/layers/" + hash + ".zip"));
                        }
                        Path archive = root.resolve(".transfer/" + job.id() + ".zip");
                        if (!job.inputSha256().equals(AutoLlmFileUtil.hash(archive))) throw new IllegalStateException("Saved workload payload changed");
                        transport.upload(remote, ".jobs/" + job.id() + "/input.zip", archive);
                        state = AutoLlmFileUtil.JSON.readValue(transport.call("submit", remote, job.id(), job), State.class);
                        verified = true;
                    } else if (!verified || state.status().equals("SUBMITTED")) {
                        state = AutoLlmFileUtil.JSON.readValue(transport.call("submit", remote, job.id(), job), State.class);
                        verified = true;
                    }
                    if (cancelled.getAsBoolean() && !cancellationSent) {
                        transport.call("cancel", remote, job.id(), Map.of()); cancellationSent = true;
                    }
                    Poll page = AutoLlmFileUtil.JSON.readValue(transport.call("poll", remote, job.id(), offsets), Poll.class);
                    for (var entry : page.chunks().entrySet()) {
                        Path file = work.output().resolve(entry.getKey()); Files.createDirectories(file.getParent());
                        long offset = Files.exists(file) ? Files.size(file) : 0;
                        if (offset != entry.getValue().offset()) throw new IllegalStateException("Remote log cursor changed");
                        byte[] bytes = Base64.getDecoder().decode(entry.getValue().base64());
                        Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND); offsets.put(entry.getKey(), offset + bytes.length);
                    }
                    for (var entry : page.snapshots().entrySet()) {
                        // Read a complete JSON snapshot before atomically exposing it to the local API.
                        try { AutoLlmFileUtil.write(WorkloadPathUtil.inside(root, entry.getKey()), AutoLlmFileUtil.JSON.readTree(entry.getValue())); }
                        catch (RuntimeException ignored) { /* A remote producer may be replacing its snapshot; retry next poll. */ }
                    }
                    state = page.state();
                    lastSynchronizedAt = Instant.now().toString();
                    publish(work.output(), job, cancellationSent ? "CANCELLATION_PENDING" : state.status(), null);
                    if (state.archiveSha256() != null) {
                        Path archive = work.output().resolve("remote-result.zip"); transport.download(remote, job.id(), archive);
                        if (!state.archiveSha256().equals(AutoLlmFileUtil.hash(archive))) throw new IllegalStateException("Remote result checksum mismatch");
                        WorkloadArchiveUtil.unpack(archive, root); Files.delete(archive);
                        AutoLlmFileUtil.write(work.output().resolve("collection-completed.json"), Map.of("jobId", job.id(), "archiveSha256", state.archiveSha256()));
                        publish(work.output(), job, state.status(), state.failure());
                        if (state.status().equals("CANCELLED")) throw new CancellationException("Remote cancellation confirmed");
                        if (state.status().equals("HOST_CHANGED")) throw new ExecutionTargetException(state.failure());
                        if (state.status().equals("INTERRUPTED")) throw new CancellationException("Remote supervisor interrupted; resume without a component repair");
                        if (state.result() == null) {
                            Path failure = work.output().resolve("execution/failure.json");
                            if (Files.exists(failure)) throw new AutoLlmFailureUtil.StageException(AutoLlmFileUtil.read(failure, AutoLlmFailureUtil.Failure.class), null);
                            throw new IllegalStateException(state.failure());
                        }
                        return state.result();
                    }
                    connectionFailures = 0;
                } catch (SshTransport.ConnectionException e) {
                    AutoLlmFileUtil.append(work.output().resolve("connection-attempts.jsonl"), Map.of("timestamp", Instant.now().toString(), "failure", e.getMessage(), "retry", connectionFailures));
                    boolean exhausted = connectionFailures++ >= AutoLlmFailureUtil.MAX_RETRIES;
                    if (!exhausted) connectionRetries++;
                    publish(work.output(), job, cancelled.getAsBoolean() ? "CANCELLATION_PENDING" : "WAITING_FOR_REMOTE", e.getMessage());
                    if (exhausted) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "REMOTE_UNREACHABLE", "Remote job " + job.id() + " is detached; resume or cancel this saved operation to reconcile it. The server watchdog still owns the workload.", descriptor);
                }
                try { Thread.sleep(2000); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Remote job detached; reconcile before resuming"); }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void publish(Path output, Job job, String state, String failure) {
        var values = new LinkedHashMap<String,Object>();
        values.put("jobId", job.id()); values.put("host", config.execution().ssh().host()); values.put("status", state);
        values.put("stageDirectory", job.output());
        values.put("connectionRetries", connectionRetries);
        values.put("remoteRoot", WorkloadPathUtil.remoteRoot(config, root)); values.put("requestedWorkers", config.execution().workers());
        values.put("updatedAt", Instant.now().toString()); values.put("failure", failure);
        values.put("lastSynchronizedAt", lastSynchronizedAt);
        AutoLlmFileUtil.write(output.resolve("remote-status.json"), values); observer.accept(values);
    }
}
