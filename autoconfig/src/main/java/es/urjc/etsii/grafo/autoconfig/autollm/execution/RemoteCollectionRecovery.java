package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.Map;

/** A compatible maintenance runner repairs collection without changing a saved workload or its supervisor. */
public final class RemoteCollectionRecovery {
    private RemoteCollectionRecovery() {}
    public static Object repair(Path descriptor, Path runner) {
        descriptor = AutoLlmNormalization.localPath(descriptor); runner = AutoLlmNormalization.localPath(runner);
        var job = AutoLlmFileUtil.read(descriptor, WorkloadProtocol.Job.class);
        var owner = ExecutionOwner.of(job); Path root = Path.of(owner.localRoot());
        if (!owner.remote() || !root.resolve(job.output()).resolve("remote-job.json").normalize().equals(descriptor)
                || !ExecutionOwner.read(root).equals(owner)) throw new IllegalArgumentException("Saved job ownership mismatch");
        String descriptorHash = AutoLlmFileUtil.hash(descriptor), runnerHash = AutoLlmFileUtil.hash(runner);
        var config = AutoLlmFileUtil.JSON.valueToTree(owner.config());
        ((tools.jackson.databind.node.ObjectNode) config.path("execution").path("ssh")).put("runnerArtifact", runner.toString());
        Path scratch = root.resolve(".collection-recovery/" + runnerHash);
        var transport = new SshTransport(AutoLlmFileUtil.JSON.treeToValue(config, AutoLlmConfig.class), scratch);
        var identity = Map.of("jobId", job.id(), "jobRoot", job.root(), "descriptorSha256", descriptorHash, "maintenanceRunnerSha256", runnerHash);
        AutoLlmFileUtil.write(scratch.resolve("intent.json"), identity);
        transport.bootstrap();
        // status can finish a dead supervisor's saved collection under its run.lock.
        // It never submits, cancels or executes a workload.
        var state = AutoLlmFileUtil.JSON.readValue(transport.call("status", job.root(), job.id(), Map.of()), WorkloadProtocol.State.class);
        if (!descriptorHash.equals(AutoLlmFileUtil.hash(descriptor))) throw new IllegalStateException("Saved job changed during collection repair");
        if (state.status().inProgress() || state.archiveSha256() == null) throw new IllegalStateException("No completed archive is available; saved job remains unresolved: " + state.status());
        var receipt = Map.of("identity", identity, "state", state, "repairedAt", Instant.now().toString());
        AutoLlmFileUtil.write(scratch.resolve("receipt.json"), receipt);
        return receipt;
    }
}
