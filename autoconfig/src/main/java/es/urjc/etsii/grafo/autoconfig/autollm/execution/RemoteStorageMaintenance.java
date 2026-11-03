package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Retryable maintenance; unavailable workers never alter completed experiment results. */
public final class RemoteStorageMaintenance {
    private RemoteStorageMaintenance() {}
    public static void maintain(Path campaign, Path root, BooleanSupplier cancelled) {
        if (CampaignAssetStore.campaign(root) == null || AutoLlmCampaignStore.hasUncollectedJobs(root) || cancelled.getAsBoolean()) return;
        Path receipt = root.resolve("storage/remote-retention.json");
        try {
            if (Files.exists(receipt) && "RELEASED".equals(AutoLlmFileUtil.read(receipt, Map.class).get("status"))) return;
            var descriptors = AutoLlmFileUtil.ownedFiles(root, "remote-job.json");
            var roots = new TreeMap<String,ExecutionOwner>();
            for (Path descriptor : descriptors) {
                var job = AutoLlmFileUtil.read(descriptor, WorkloadProtocol.Job.class);
                if (!job.managedStorage()) continue;
                if (cancelled.getAsBoolean()) return;
                var owner = ExecutionOwner.of(job); roots.put(owner.workspaceId() + ":" + job.root(), owner);
                var transport = owner.transport(); transport.bootstrap();
                Path acknowledged = descriptor.resolveSibling("remote-acknowledged.json");
                if (Files.isRegularFile(acknowledged)) continue;
                var collection = AutoLlmFileUtil.read(descriptor.resolveSibling("collection-completed.json"), RemoteCampaignStorage.Collected.class);
                if (collection.version() != 1 || !job.id().equals(collection.jobId())) throw new IOException("Incomplete collection receipt: " + descriptor);
                RemoteCampaignStorage.verifyCollection(root, collection);
                var request = new RemoteCampaignStorage.Acknowledgement(job.id(), collection.archiveSha256(), AutoLlmFileUtil.canonicalHash(collection.entries()));
                var response = AutoLlmFileUtil.JSON.readTree(transport.call("acknowledge", job.root(), job.id(), request));
                if (!response.path("acknowledged").asBoolean() || !job.id().equals(response.path("jobId").asString())) throw new IOException("Invalid remote acknowledgement");
                AutoLlmFileUtil.write(acknowledged, request); RemoteCampaignStorage.force(acknowledged);
                Files.deleteIfExists(descriptor.resolveSibling("acknowledgement-pending.json"));
                Files.deleteIfExists(root.resolve(".transfer/" + job.id() + ".zip"));
            }
            var released = new TreeMap<String,Object>();
            for (var entry : roots.entrySet()) {
                String remote = entry.getValue().executionRoot(); var transport = entry.getValue().transport(); transport.bootstrap();
                if (cancelled.getAsBoolean()) return;
                var response = AutoLlmFileUtil.JSON.readValue(Objects.requireNonNull(transport).call("retire", remote, null, Map.of()), Map.class);
                if (!Boolean.TRUE.equals(response.get("retired"))) throw new IOException("Remote retirement not confirmed");
                released.put(remote, response);
            }
            CampaignAssetStore.releaseLayers(campaign, root);
            AutoLlmFileUtil.write(receipt, Map.of("status", "RELEASED", "roots", released, "observedAt", Instant.now().toString()));
        } catch (IOException | RuntimeException failure) {
            AutoLlmFileUtil.write(receipt, Map.of("status", "RETAINED", "reason", failure.toString(), "observedAt", Instant.now().toString()));
        }
    }
}
