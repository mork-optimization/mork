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
    private Instant disconnectedAt;
    private Instant nextRetryAt;
    private int connectionFailures;
    public SshWorkloadExecutor(AutoLlmConfig config, Path root, SshTransport transport, Consumer<Map<String,Object>> observer) {
        this.config = config; this.root = root; this.transport = transport; this.observer = observer;
    }
    @Override public AutoLlmProcessRunner.Result run(Workload work, BooleanSupplier cancelled) {
        if (!Files.isRegularFile(root.resolve("execution-owner.json"))) ExecutionOwner.waitFor(config, root);
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        Path descriptor = work.output().resolve("remote-job.json");
        Path campaign = CampaignAssetStore.campaign(root);
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
                var data = new TreeMap<String,Path>(); var bundles = new TreeMap<String,Path>(); var executables = new TreeMap<String,Path>(); var mutable = new TreeMap<String,Path>();
                for (var entry : work.inputs().entrySet()) {
                    if (entry.getKey().startsWith("data/inputs/")) data.put(entry.getKey(), entry.getValue());
                    else if (entry.getKey().startsWith(".source-bundles/") && entry.getKey().endsWith(".bundle")) bundles.put(entry.getKey(), entry.getValue());
                    else if (entry.getKey().equals("frozen/project.jar") || campaign != null && entry.getKey().endsWith(".jar")) executables.put(entry.getKey(), entry.getValue());
                    else mutable.put(entry.getKey(), entry.getValue());
                }
                var layers = new ArrayList<String>();
                for (var immutable : List.of(data, bundles, executables)) if (!immutable.isEmpty()) {
                    String hash = campaign == null ? WorkloadArchiveUtil.prepareLayer(root.resolve(".transfer/layers"), immutable) : CampaignAssetStore.layer(campaign, work.output(), immutable);
                    layers.add((campaign == null ? ".layers/" : ".campaign-layers/") + hash + ".zip");
                }
                Path archive = root.resolve(".transfer/" + id + ".zip"); WorkloadArchiveUtil.pack(archive, mutable);
                var command = new ArrayList<String>();
                for (String argument : work.command()) command.add(WorkloadPathUtil.argument(argument, root, remote));
                if (work.command().getFirst().equals(Path.of(System.getProperty("java.home"), "bin/java").toString()) || work.command().getFirst().equals("java")) {
                    command.set(0, config.execution().ssh().onlyServer().javaExecutable()); command.add(1, "-Dmork.execution.remote=true");
                }
                var reset = new ArrayList<>(work.reset()); reset.add(name);
                job = new Job(VERSION, id, remote, AutoLlmFileUtil.hash(archive), layers, command, root.relativize(work.cwd()).toString().replace('\\', '/'), name, work.collect(), reset,
                        config.execution().ssh().onlyServer().environment(), Files.exists(root.resolve("execution-target.json")) ? AutoLlmFileUtil.read(root.resolve("execution-target.json"), Host.class).identity() : Map.of(),
                        work.timeoutMillis(), work.plannedMillis(), work.graceMillis(), work.watchEvaluations(), work.startupMillis(), campaign != null, ExecutionOwner.read(root));
                AutoLlmFileUtil.write(descriptor, job); // durable identity before any uncertain remote submission
            }
            if (!remote.equals(job.root())) throw new ExecutionTargetException("Remote workspace changed; reconcile the original job before configuring another target");
            Path collection = work.output().resolve("collection-completed.json");
            if (job.managedStorage() && Files.isRegularFile(collection)) {
                var saved = AutoLlmFileUtil.read(collection, RemoteCampaignStorage.Collected.class);
                if (saved.version() != 1 || !job.id().equals(saved.jobId())) throw new IOException("Invalid local collection receipt");
                if (!Files.isRegularFile(work.output().resolve("remote-acknowledged.json"))) RemoteCampaignStorage.verifyCollection(root, saved);
                acknowledge(work.output(), job, saved, false);
                transport.bootstrap(); transport.call("job-collected", remote, job.id(), Map.of());
                publish(work.output(), job, saved.state().status().name(), saved.state().failure());
                return completed(saved.state(), work.output());
            }
            Path previous = work.output().resolve("remote-status.json");
            if (Files.exists(previous)) {
                var saved = AutoLlmFileUtil.JSON.readTree(Files.readString(previous));
                if (!saved.path("disconnectedAt").isNull() && !saved.path("disconnectedAt").isMissingNode()) disconnectedAt = Instant.parse(saved.path("disconnectedAt").asString());
                connectionFailures = saved.path("consecutiveFailures").asInt(0);
                if (!saved.path("nextRetryAt").isNull() && !saved.path("nextRetryAt").isMissingNode()) nextRetryAt = Instant.parse(saved.path("nextRetryAt").asString());
                lastSynchronizedAt = saved.path("lastSynchronizedAt").asString(null); connectionRetries = saved.path("connectionRetries").asInt(0);
            }
            var offsets = new HashMap<String,Long>();
            for (String name : List.of("stdout.log", "stderr.log", "execution/events.jsonl")) {
                Path file = work.output().resolve(name); offsets.put(name, Files.exists(file) ? Files.size(file) : 0L);
            }
            boolean cancellationSent = false; boolean cancellationAttempted = false; boolean initialized = false; boolean verified = false;

            while (true) {
                if (Thread.currentThread().isInterrupted()) throw new CancellationException("Remote job detached; resume the saved job identity");
                try {
                    if (nextRetryAt != null) {
                        while (Instant.now().isBefore(nextRetryAt)) {
                            if (cancelled.getAsBoolean() && !cancellationAttempted) break;
                            Thread.sleep(Math.min(200, Math.max(1, java.time.Duration.between(Instant.now(), nextRetryAt).toMillis())));
                        }
                    }
                    cancellationAttempted |= cancelled.getAsBoolean();
                    if (!initialized) { transport.bootstrap(); initialized = true; }
                    State state = AutoLlmFileUtil.JSON.readValue(transport.call("status", remote, job.id(), Map.of()), State.class);
                    if (state.status() == JobStatus.NOT_FOUND) {
                        if (lastSynchronizedAt != null) throw new ExecutionTargetException("Previously observed remote job disappeared; inspect its identity before any replacement");
                        if (job.managedStorage()) transport.call("retain-layers", remote, job.id(), job.layers());
                        for (String layer : job.layers()) {
                            String hash = Path.of(layer).getFileName().toString().replace(".zip", "");
                            var cached = AutoLlmFileUtil.JSON.readTree(transport.call("has", remote, layer, Map.of()));
                            if (!hash.equals(cached.path("sha256").asString())) transport.upload(remote, layer, (job.managedStorage() ? Objects.requireNonNull(campaign).resolve(".store/layers") : root.resolve(".transfer/layers")).resolve(hash + ".zip"));
                        }
                        Path archive = root.resolve(".transfer/" + job.id() + ".zip");
                        if (!job.inputSha256().equals(AutoLlmFileUtil.hash(archive))) throw new IllegalStateException("Saved workload payload changed");
                        transport.upload(remote, ".jobs/" + job.id() + "/input.zip", archive);
                        state = AutoLlmFileUtil.JSON.readValue(transport.call("submit", remote, job.id(), job), State.class);
                        verified = true;
                    } else if (!verified || state.status() == JobStatus.SUBMITTED) {
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
                        try {
                            Path snapshot = WorkloadPathUtil.inside(root, entry.getKey());
                            var value = AutoLlmFileUtil.JSON.readTree(entry.getValue());
                            if (!Files.isRegularFile(snapshot) || !value.equals(AutoLlmFileUtil.JSON.readTree(Files.readString(snapshot)))) AutoLlmFileUtil.write(snapshot, value);
                        }
                        catch (RuntimeException ignored) { /* A remote producer may be replacing its snapshot; retry next poll. */ }
                    }
                    state = page.state();
                    lastSynchronizedAt = Instant.now().toString();
                    if (state.archiveSha256() != null) {
                        if (AutoLlmCleanup.requested(root)) {
                            transport.call("job-collected", remote, job.id(), Map.of());
                            AutoLlmFileUtil.write(work.output().resolve("collection-discarded.json"), Map.of("jobId", job.id(), "reason", "Reviewed complete cleanup"));
                            throw new CancellationException("Cleanup confirmed termination and discarded disposable results");
                        }
                        publish(work.output(), job, cancellationSent ? "CANCELLATION_PENDING" : "COLLECTING", null);
                        long downloadStart = System.nanoTime();
                        Path archive = work.output().resolve("remote-result.zip"); transport.download(remote, job.id(), archive);
                        long archiveBytes = Files.size(archive), downloadMillis = (System.nanoTime() - downloadStart) / 1_000_000;
                        if (!state.archiveSha256().equals(AutoLlmFileUtil.hash(archive))) throw new IllegalStateException("Remote result checksum mismatch");
                        var manifest = job.managedStorage() ? WorkloadArchiveUtil.verify(archive, root) : Map.<String,WorkloadArchiveUtil.Entry>of();
                        WorkloadArchiveUtil.unpack(archive, root);
                        if (job.managedStorage()) {
                            if (!WorkloadArchiveUtil.matches(root, manifest)) throw new IOException("Collected files do not match remote evidence");
                            for (var item : manifest.entrySet()) if (item.getValue().link() == null) RemoteCampaignStorage.force(WorkloadPathUtil.inside(root, item.getKey()));
                        }
                        DirectoryArchiveUtil.archive(work.output().resolve("solutions"));
                        if (job.managedStorage()) {
                            var receipt = new RemoteCampaignStorage.Collected(1, job.id(), state.archiveSha256(), archiveBytes, downloadMillis, (System.nanoTime() - downloadStart) / 1_000_000, manifest, state);
                            RemoteCampaignStorage.verifyCollection(root, receipt);
                            AutoLlmFileUtil.write(collection, receipt); RemoteCampaignStorage.force(collection);
                            acknowledge(work.output(), job, receipt, true);
                        } else AutoLlmFileUtil.write(collection, Map.of("jobId", job.id(), "archiveSha256", state.archiveSha256(), "archiveBytes", archiveBytes, "downloadMillis", downloadMillis, "collectionMillis", (System.nanoTime() - downloadStart) / 1_000_000));
                        transport.call("job-collected", remote, job.id(), Map.of());
                        Files.delete(archive);
                        disconnectedAt = null; nextRetryAt = null; connectionFailures = 0;
                        publish(work.output(), job, state.status().name(), state.failure());
                        return completed(state, work.output());
                    }
                    connectionFailures = 0; disconnectedAt = null; nextRetryAt = null;
                    publish(work.output(), job, cancellationSent ? "CANCELLATION_PENDING" : state.status().name(), null);
                } catch (SshTransport.ConnectionException e) {
                    AutoLlmFileUtil.append(work.output().resolve("connection-attempts.jsonl"), Map.of("timestamp", Instant.now().toString(), "failure", e.getMessage(), "retry", connectionFailures));
                    if (disconnectedAt == null) disconnectedAt = Instant.now();
                    connectionFailures++; connectionRetries++;
                    var policy = config.execution().ssh().reconnect();
                    boolean exhausted = java.time.Duration.between(disconnectedAt, Instant.now()).toMillis() >= policy.budgetMillis();
                    nextRetryAt = Instant.now().plusMillis(Math.min(policy.delay(connectionFailures), Math.max(1, policy.budgetMillis() - java.time.Duration.between(disconnectedAt, Instant.now()).toMillis())));
                    publish(work.output(), job, cancelled.getAsBoolean() ? "CANCELLATION_PENDING" : "WAITING_FOR_REMOTE", e.getMessage());
                    if (exhausted) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "REMOTE_UNREACHABLE", "Remote job " + job.id() + " is detached; resume or cancel this saved operation to reconcile it. The server watchdog still owns the workload.", descriptor);
                }
                try { Thread.sleep(nextRetryAt == null ? 2000 : 1); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Remote job detached; reconcile before resuming"); }
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("SSH reconnect interrupted; saved job retained"); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void acknowledge(Path output, Job job, RemoteCampaignStorage.Collected receipt, boolean connected) {
        Path confirmed = output.resolve("remote-acknowledged.json");
        if (Files.isRegularFile(confirmed)) return;
        try {
            if (!connected) transport.bootstrap();
            RemoteCampaignStorage.verifyCollection(root, receipt);
            var request = new RemoteCampaignStorage.Acknowledgement(job.id(), receipt.archiveSha256(), AutoLlmFileUtil.canonicalHash(receipt.entries()));
            var response = AutoLlmFileUtil.JSON.readTree(transport.call("acknowledge", job.root(), job.id(), request));
            if (!response.path("acknowledged").asBoolean() || !job.id().equals(response.path("jobId").asString())) throw new IllegalStateException("Remote acknowledgement identity mismatch");
            AutoLlmFileUtil.write(confirmed, request); RemoteCampaignStorage.force(confirmed);
            Files.deleteIfExists(output.resolve("acknowledgement-pending.json"));
            Files.deleteIfExists(root.resolve(".transfer/" + job.id() + ".zip"));
        } catch (IOException | RuntimeException failure) {
            AutoLlmFileUtil.write(output.resolve("acknowledgement-pending.json"), Map.of("jobId", job.id(), "reason", failure.toString()));
        }
    }
    private static AutoLlmProcessRunner.Result completed(State state, Path output) {
        if (state.status() == JobStatus.CANCELLED) throw new CancellationException("Remote cancellation confirmed");
        if (state.status() == JobStatus.HOST_CHANGED) throw new ExecutionTargetException(state.failure());
        if (state.status() == JobStatus.INTERRUPTED) throw new CancellationException("Remote supervisor interrupted; resume without a component repair");
        if (state.result() == null) {
            Path failure = output.resolve("execution/failure.json");
            if (Files.exists(failure)) throw new AutoLlmFailureUtil.StageException(AutoLlmFileUtil.read(failure, AutoLlmFailureUtil.Failure.class), null);
            throw new IllegalStateException(state.failure());
        }
        return state.result();
    }
    private void publish(Path output, Job job, String state, String failure) {
        var values = new LinkedHashMap<String,Object>();
        values.put("jobId", job.id()); values.put("host", config.execution().ssh().onlyServer().host()); values.put("status", state);
        values.put("stageDirectory", job.output());
        values.put("connectionRetries", connectionRetries);
        values.put("remoteRoot", WorkloadPathUtil.remoteRoot(config, root)); values.put("requestedWorkers", config.execution().workers());
        values.put("updatedAt", Instant.now().toString()); values.put("failure", failure);
        values.put("lastSynchronizedAt", lastSynchronizedAt); values.put("lastContactAt", lastSynchronizedAt);
        values.put("assignmentId", job.owner().assignment()); values.put("workerId", job.owner().lease().workerId()); values.put("lease", job.owner().lease());
        values.put("disconnectedAt", disconnectedAt == null ? null : disconnectedAt.toString());
        values.put("nextRetryAt", nextRetryAt == null ? null : nextRetryAt.toString());
        values.put("consecutiveFailures", connectionFailures);
        AutoLlmFileUtil.write(output.resolve("remote-status.json"), values); observer.accept(values);
    }
}
