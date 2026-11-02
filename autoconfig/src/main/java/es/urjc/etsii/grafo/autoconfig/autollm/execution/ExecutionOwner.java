package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.nio.file.*;
import java.util.*;

/** Frozen endpoint and reservation; never reconstructed from a trial's current target. */
public record ExecutionOwner(int version, String operation, String assignment, String localRoot, String executionRoot,
                             AutoLlmConfig config, WorkerRegistry.Lease lease) {
    public static ExecutionOwner read(Path root) {
        var owner = AutoLlmFileUtil.read(root.resolve("execution-owner.json"), ExecutionOwner.class);
        if (owner.version != WorkloadProtocol.VERSION || !AutoLlmNormalization.localPath(Path.of(owner.localRoot)).equals(AutoLlmNormalization.localPath(root)))
            throw new IllegalStateException("Execution ownership mismatch: " + root);
        return owner;
    }
    public static ExecutionOwner waitFor(AutoLlmConfig config, Path root) {
        while (true) {
            if (AutoLlmCleanup.requested(root)) throw new java.util.concurrent.CancellationException("Cleanup requested");
            try { return reserve(config, root); }
            catch (WorkerRegistry.Busy busy) {
                AutoLlmFileUtil.write(root.resolve("worker-availability.json"), busy.owner);
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new java.util.concurrent.CancellationException("Waiting for worker interrupted"); }
            }
        }
    }
    public static synchronized ExecutionOwner reserve(AutoLlmConfig config, Path root) {
        root = AutoLlmNormalization.localPath(root);
        OwnershipManifest.directory(root, "execution", root.toString());
        Path file = root.resolve("execution-owner.json");
        if (Files.isRegularFile(root.resolve("lease-released.json"))) {
            var previous = read(root);
            AutoLlmFileUtil.write(root.resolve("ownership-history/" + previous.lease.token() + ".json"), previous);
            try { Files.delete(file); Files.delete(root.resolve("lease-released.json")); }
            catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
        }
        if (Files.isRegularFile(file)) {
            var owner = read(root);
            if (!AutoLlmNormalization.normalize(owner.config).execution().equals(AutoLlmNormalization.normalize(config).execution())) throw new IllegalStateException("Execution target changed");
            if (owner.lease.generation() > 0) { owner.validate(); return owner.remote() && !Files.isRegularFile(root.resolve("remote-assets.json")) ? register(owner, file) : owner; }
            return acquire(owner, file);
        }
        Map<?,?> context = Files.isRegularFile(root.resolve("execution-context.json")) ? AutoLlmFileUtil.read(root.resolve("execution-context.json"), Map.class) : Map.of();
        String operation = Objects.toString(context.get("campaignId"), root.toString());
        String assignment = Objects.toString(context.get("assignment"), null);
        String controller = WorkerRegistry.system().status().workerId() + ":" + AutoLlmNormalization.localPath(Path.of(config.workspace()));
        var lease = new WorkerRegistry.Lease(null, controller, root.toString(), assignment, UUID.randomUUID().toString(), 0);
        var owner = new ExecutionOwner(WorkloadProtocol.VERSION, operation, assignment, root.toString(),
                config.execution().target() == AutoLlmConfig.Target.SSH ? WorkloadPathUtil.remoteRoot(config, root) : root.toString(), config, lease);
        AutoLlmFileUtil.write(file, owner); // intent survives a lost acquisition response
        return acquire(owner, file);
    }
    private static ExecutionOwner acquire(ExecutionOwner owner, Path file) {
        WorkerRegistry.Lease lease;
        if (owner.remote()) {
            var result = AutoLlmFileUtil.JSON.readTree(owner.call("lease-acquire", owner.lease));
            if (result.has("busy")) throw new WorkerRegistry.Busy(AutoLlmFileUtil.JSON.treeToValue(result.path("busy"), WorkerRegistry.Status.class));
            lease = AutoLlmFileUtil.JSON.treeToValue(result, WorkerRegistry.Lease.class);
        } else lease = WorkerRegistry.system().acquire(owner.lease);
        var acquired = new ExecutionOwner(owner.version, owner.operation, owner.assignment, owner.localRoot, owner.executionRoot, owner.config, lease);
        AutoLlmFileUtil.write(file, acquired);
        return register(acquired, file);
    }
    private static ExecutionOwner register(ExecutionOwner owner, Path file) {
        if (owner.remote()) {
            var resource = AutoLlmFileUtil.JSON.readValue(owner.call("ownership-register", Map.of("reference", owner.reference())), OwnershipManifest.Resource.class);
            owner = new ExecutionOwner(owner.version, owner.operation, owner.assignment, owner.localRoot, resource.path(), owner.config, owner.lease);
            AutoLlmFileUtil.write(file, owner); AutoLlmFileUtil.write(Path.of(owner.localRoot).resolve("remote-resource.json"), resource);
            AutoLlmFileUtil.write(Path.of(owner.localRoot).resolve("remote-assets.json"), AutoLlmFileUtil.JSON.readTree(owner.call("ownership-plan", Map.of())));
        }
        return owner;
    }
    public String reference() { return lease.controller() + ":" + localRoot; }
    public boolean remote() { return config.execution().target() == AutoLlmConfig.Target.SSH; }
    public SshTransport transport() { return new SshTransport(config, Path.of(localRoot).resolve(".transport")); }
    public byte[] call(String action, Object payload) { var t = transport(); t.bootstrap(); return t.call(action, executionRoot, null, payload); }
    public void validate() { if (remote()) call("lease-validate", lease); else WorkerRegistry.system().validate(lease); }
    public void release() {
        var released = lease;
        if (lease.generation() == 0) {
            var status = remote() ? AutoLlmFileUtil.JSON.readValue(call("worker-status", Map.of()), WorkerRegistry.Status.class) : WorkerRegistry.system().status();
            released = status.lease() != null && status.lease().token().equals(lease.token()) ? status.lease() : null;
        }
        if (released != null) {
            if (remote()) call("lease-release", released); else WorkerRegistry.system().release(released);
        }
        AutoLlmFileUtil.write(Path.of(localRoot).resolve("lease-released.json"), lease);
    }
    public static void releaseIfPresent(Path root) {
        if (Files.isRegularFile(root.resolve("execution-owner.json")) && !Files.exists(root.resolve("lease-released.json"))) read(root).release();
    }
    public static ExecutionOwner of(WorkloadProtocol.Job job) {
        if (job.version() != WorkloadProtocol.VERSION || job.owner() == null || !job.root().equals(job.owner().executionRoot()))
            throw new IllegalStateException("Job has no valid version-7 owner");
        return job.owner();
    }
}
