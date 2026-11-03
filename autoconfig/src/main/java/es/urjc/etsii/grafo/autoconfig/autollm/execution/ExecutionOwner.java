package es.urjc.etsii.grafo.autoconfig.autollm.execution;
import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.nio.file.*;
import java.util.*;

/** Frozen execution endpoint; workspace ownership is independent of execution locks. */
public record ExecutionOwner(int version, String operation, String assignment, String localRoot, String executionRoot,
                             AutoLlmConfig config, String controllerWorkspaceId, String workspaceId) {
    public record Inventory(String workspaceId, List<OwnershipManifest.Resource> resources) {}
    public static ExecutionOwner read(Path root) {
        var owner = AutoLlmFileUtil.read(root.resolve("execution-owner.json"), ExecutionOwner.class);
        if (owner.version != WorkloadProtocol.VERSION || !AutoLlmNormalization.localPath(Path.of(owner.localRoot)).equals(AutoLlmNormalization.localPath(root)))
            throw new IllegalStateException("Execution ownership mismatch: " + root);
        return owner;
    }
    public static synchronized ExecutionOwner prepare(AutoLlmConfig config, Path root) {
        root = AutoLlmNormalization.localPath(root);
        new OwnershipManifest(Path.of(config.workspace())).directory(root, "execution", root.toString());
        Path file = root.resolve("execution-owner.json");
        if (Files.isRegularFile(file)) {
            var owner = read(root);
            if (!AutoLlmNormalization.normalize(owner.config).execution().equals(AutoLlmNormalization.normalize(config).execution())) throw new IllegalStateException("Execution target changed");
            return owner.remote() && (owner.workspaceId == null || !Files.isRegularFile(root.resolve("remote-assets.json"))) ? register(owner, file) : owner;
        }
        Map<?,?> context = Files.isRegularFile(root.resolve("execution-context.json")) ? AutoLlmFileUtil.read(root.resolve("execution-context.json"), Map.class) : Map.of();
        String controller = WorkspaceState.identity(Path.of(config.workspace()));
        var owner = new ExecutionOwner(WorkloadProtocol.VERSION, Objects.toString(context.get("campaignId"), root.toString()), Objects.toString(context.get("assignment"), null), root.toString(),
            config.execution().target() == AutoLlmConfig.Target.SSH ? WorkloadPathUtil.remoteRoot(config, root) : root.toString(), config, controller, config.execution().target() == AutoLlmConfig.Target.SSH ? null : controller);
        AutoLlmFileUtil.write(file, owner); // Preserve the endpoint before registration can have an uncertain response.
        return register(owner, file);
    }
    private static ExecutionOwner register(ExecutionOwner owner, Path file) {
        Path root = Path.of(owner.localRoot);
        if (owner.remote()) {
            var response = AutoLlmFileUtil.JSON.readTree(owner.call("ownership-register", Map.of("reference", owner.reference())));
            var resource = AutoLlmFileUtil.JSON.treeToValue(response.path("resource"), OwnershipManifest.Resource.class);
            if (owner.workspaceId != null && !owner.workspaceId.equals(response.path("workspaceId").asString()))
                throw new ExecutionTargetException("Remote workspace identity changed");
            owner = new ExecutionOwner(owner.version, owner.operation, owner.assignment, owner.localRoot, resource.path(), owner.config, owner.controllerWorkspaceId, response.path("workspaceId").asString());
            AutoLlmFileUtil.write(file, owner);
            AutoLlmFileUtil.write(root.resolve("remote-resource.json"), resource);
            AutoLlmFileUtil.write(root.resolve("remote-assets.json"), AutoLlmFileUtil.JSON.readTree(owner.call("ownership-plan", Map.of())));
        }
        return owner;
    }
    public String reference() { return controllerWorkspaceId + ":" + localRoot; }
    public boolean remote() { return config.execution().target() == AutoLlmConfig.Target.SSH; }
    public SshTransport transport() { return new SshTransport(config, Path.of(localRoot).resolve(".transport")); }
    public byte[] call(String action, Object payload) { var t = transport(); t.bootstrap(); return t.call(action, executionRoot, null, payload); }
    public static ExecutionOwner of(WorkloadProtocol.Job job) {
        if (job.version() != WorkloadProtocol.VERSION || job.owner() == null || job.owner().version() != WorkloadProtocol.VERSION || job.owner().workspaceId() == null || !job.root().equals(job.owner().executionRoot())) throw new IllegalStateException("Job ownership or protocol mismatch");
        return job.owner();
    }
}
