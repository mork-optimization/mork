package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Saved prerequisite diagnostics, with a bounded pool independent of measured campaign work. */
public class AutoLlmTargetProbeService implements AutoCloseable {
    public enum ProbeStatus { RUNNING, SUCCEEDED, COMPLETED_WITH_FAILURES, INTERRUPTED }
    public enum TargetStatus {
        QUEUED, RUNNING, FAILED, SUCCEEDED, INTERRUPTED;
        public boolean pending() { return this == QUEUED || this == RUNNING; }
    }
    public record Target(TargetStatus status, WorkloadProtocol.Host host, AutoLlmFailureUtil.Failure problem) {}
    public record Probe(String id, String createdAt, ProbeStatus status, Map<String,Target> targets) {}
    private record Saved(Probe probe, long pid, String processStartedAt) {}
    private static final int PARALLEL_TARGETS = 4;
    private final Path root;
    private final AutoLlmExecutionService workloads;
    private final ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_TARGETS, Thread.ofVirtual().name("target-preflight-", 0).factory());
    private final Map<String,Probe> active = new HashMap<>();
    private final long pid = ProcessHandle.current().pid();
    private final String processStartedAt = ProcessHandle.current().info().startInstant().orElseThrow().toString();
    private boolean closed;

    public AutoLlmTargetProbeService(AutoLlmCampaignStore store, AutoLlmProcessRunner processes) {
        root = store.root().resolve("target-probes"); workloads = new AutoLlmExecutionService(processes);
    }
    private Path directory(String id) {
        if (!id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid target probe id");
        return root.resolve(id);
    }
    private void save(Probe probe) { AutoLlmFileUtil.write(directory(probe.id()).resolve("probe.json"), new Saved(probe, pid, processStartedAt)); }
    public synchronized Probe start(AutoLlmConfig config) {
        if (closed) throw new IllegalStateException("Target preflight is shutting down");
        var errors = AutoLlmExecutionService.validate(config);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var definitions = new LinkedHashMap<String,AutoLlmConfig>();
        if (config.execution().target() == AutoLlmConfig.Target.LOCAL) definitions.put("local", config);
        else for (var server : config.execution().ssh().servers()) definitions.put(server.id(), config.forServer(server));
        var targets = new LinkedHashMap<String,Target>();
        for (String id : definitions.keySet()) targets.put(id, new Target(TargetStatus.QUEUED, null, null));
        var probe = new Probe(UUID.randomUUID().toString(), Instant.now().toString(), ProbeStatus.RUNNING, Collections.unmodifiableMap(targets));
        save(probe); active.put(probe.id(), probe);
        for (var entry : definitions.entrySet()) executor.execute(() -> run(probe.id(), entry.getKey(), entry.getValue(),
                config.execution().target() == AutoLlmConfig.Target.SSH && entry.getKey().equals(config.execution().ssh().screeningServerId()) ? "" : null));
        return probe;
    }
    protected WorkloadProtocol.Host inspect(AutoLlmConfig config, Path directory, String selectorPython) {
        return workloads.inspect(config, directory, selectorPython);
    }
    private void run(String probeId, String targetId, AutoLlmConfig config, String selectorPython) {
        if (!update(probeId, targetId, new Target(TargetStatus.RUNNING, null, null))) return;
        Path directory = directory(probeId).resolve("targets/" + targetId);
        Target result;
        try {
            AutoLlmFileUtil.write(directory.resolve("execution-context.json"), AutoLlmPoolUtil.identity(probeId, targetId));
            var host = inspect(config, directory, selectorPython);
            boolean failed = false;
            for (var check : host.prerequisites()) failed |= check.status() == TargetPreflightUtil.PrerequisiteStatus.FAILED;
            result = new Target(failed ? TargetStatus.FAILED : TargetStatus.SUCCEEDED, host, null);
        } catch (CancellationException e) {
            result = new Target(TargetStatus.INTERRUPTED, null, null);
        } catch (RuntimeException e) {
            result = new Target(TargetStatus.FAILED, null, AutoLlmFailureUtil.describe(e, directory));
        }
        update(probeId, targetId, result);
    }
    private synchronized boolean update(String id, String targetId, Target result) {
        var previous = active.get(id);
        if (previous == null || closed) return false;
        var targets = new LinkedHashMap<>(previous.targets()); targets.put(targetId, result);
        boolean pending = false, failed = false;
        for (var target : targets.values()) {
            pending |= target.status().pending();
            failed |= target.status() != TargetStatus.SUCCEEDED;
        }
        var probe = new Probe(id, previous.createdAt(), pending ? ProbeStatus.RUNNING : failed ? ProbeStatus.COMPLETED_WITH_FAILURES : ProbeStatus.SUCCEEDED, Collections.unmodifiableMap(targets));
        save(probe);
        if (pending) active.put(id, probe); else active.remove(id);
        return true;
    }
    public synchronized Probe load(String id) {
        Path file = directory(id).resolve("probe.json");
        if (!Files.isRegularFile(file)) throw new NoSuchElementException("Target probe unavailable: " + id);
        var saved = AutoLlmFileUtil.read(file, Saved.class);
        if (saved.probe().status() == ProbeStatus.RUNNING) {
            boolean alive = ProcessHandle.of(saved.pid()).filter(ProcessHandle::isAlive)
                    .flatMap(process -> process.info().startInstant()).map(Instant::toString).orElse("").equals(saved.processStartedAt());
            if (!alive) {
                var interrupted = interrupted(saved.probe()); save(interrupted); return interrupted;
            }
        }
        return saved.probe();
    }
    private Probe interrupted(Probe previous) {
        var targets = new LinkedHashMap<>(previous.targets());
        for (var entry : targets.entrySet()) if (entry.getValue().status().pending()) entry.setValue(new Target(TargetStatus.INTERRUPTED, null, null));
        return new Probe(previous.id(), previous.createdAt(), ProbeStatus.INTERRUPTED, Collections.unmodifiableMap(targets));
    }
    @Override public synchronized void close() {
        closed = true; executor.shutdownNow();
        for (var probe : active.values()) save(interrupted(probe));
        active.clear();
    }
}
