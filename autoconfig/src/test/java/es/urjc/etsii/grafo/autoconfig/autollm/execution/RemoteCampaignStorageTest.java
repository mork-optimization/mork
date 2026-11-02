package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.*;
import static org.junit.jupiter.api.Assertions.*;

class RemoteCampaignStorageTest {
    @TempDir Path temp;
    private final String id = "a".repeat(64);
    private Path root, directory;
    private Job job;
    private RemoteCampaignStorage.Acknowledgement prepare() throws Exception {
        root = temp.resolve("worker/campaigns/campaign/targets/server/trials/001"); directory = root.resolve(".jobs/" + id);
        Files.createDirectories(directory); Files.createDirectories(root.resolve("source")); Files.createDirectories(root.resolve("output"));
        Files.writeString(root.resolve("source/code"), "source"); Files.writeString(root.resolve("output/events.jsonl"), "unique measurements");
        WorkloadArchiveUtil.pack(directory.resolve("input.zip"), Map.of("source/code", root.resolve("source/code")));
        WorkloadArchiveUtil.pack(directory.resolve("result.zip"), Map.of("output/events.jsonl", root.resolve("output/events.jsonl")));
        job = new Job(VERSION, id, root.toString(), AutoLlmFileUtil.hash(directory.resolve("input.zip")), List.of(), List.of("unused"), "source", "output",
                List.of(), List.of(), Map.of(), Map.of(), 100, 0, 10, false, 0, true);
        AutoLlmFileUtil.write(directory.resolve("job.json"), job);
        String hash = AutoLlmFileUtil.hash(directory.resolve("result.zip"));
        AutoLlmFileUtil.write(directory.resolve("state.json"), new State(id, JobStatus.SUCCEEDED, new AutoLlmProcessRunner.Result(0, false, 1), null, hash));
        RemoteCampaignStorage.retainLayers(root, List.of());
        return new RemoteCampaignStorage.Acknowledgement(id, hash, AutoLlmFileUtil.canonicalHash(WorkloadArchiveUtil.verify(directory.resolve("result.zip"), root)));
    }
    @Test void acknowledgementReleasesOnlyMatchingCompletedArchivesAndIsIdempotent() throws Exception {
        var receipt = prepare();
        var invalid = new RemoteCampaignStorage.Acknowledgement(id, "0".repeat(64), receipt.manifestSha256());
        assertThrows(java.io.IOException.class, () -> RemoteCampaignStorage.acknowledge(root, id, invalid));
        assertTrue(Files.exists(directory.resolve("result.zip"))); assertTrue(Files.exists(directory.resolve("input.zip")));
        RemoteCampaignStorage.acknowledge(root, id, receipt);
        assertFalse(Files.exists(directory.resolve("result.zip"))); assertFalse(Files.exists(directory.resolve("input.zip")));
        assertTrue(Files.exists(root.resolve("output/events.jsonl")), "Raw outputs remain until the owner is terminal");
        RemoteCampaignStorage.acknowledge(root, id, receipt);
        assertThrows(java.io.IOException.class, () -> RemoteCampaignStorage.acknowledge(root, id, invalid));
        assertEquals(JobStatus.SUCCEEDED, AutoLlmFileUtil.read(directory.resolve("state.json"), State.class).status());
    }
    @Test void retirementRequiresEveryAckAndPreservesUnknownOrChangedFiles() throws Exception {
        var receipt = prepare();
        assertThrows(java.io.IOException.class, () -> RemoteCampaignStorage.retire(root));
        RemoteCampaignStorage.acknowledge(root, id, receipt);
        Files.writeString(root.resolve("source/code"), "changed source"); Files.writeString(root.resolve("unknown.log"), "unique diagnostics");
        RemoteCampaignStorage.retire(root);
        assertFalse(Files.exists(root.resolve("output/events.jsonl")));
        assertEquals("changed source", Files.readString(root.resolve("source/code")));
        assertEquals("unique diagnostics", Files.readString(root.resolve("unknown.log")));
        assertTrue(Files.exists(directory.resolve("job.json"))); assertTrue(Files.exists(directory.resolve("acknowledged.json")));
        RemoteCampaignStorage.retire(root);
        assertThrows(java.io.IOException.class, () -> RemoteCampaignStorage.retainLayers(root, List.of()));
    }
    @Test void sharedRemoteLayerIsRetainedForAnotherOwner() throws Exception {
        var receipt = prepare();
        Path other = root.resolveSibling("002"); Files.createDirectories(other);
        Path payload = temp.resolve("dataset"); Files.writeString(payload, "input");
        Path store = WorkloadPathUtil.remoteCampaign(root).resolve(".store/layers");
        String hash = WorkloadArchiveUtil.prepareLayer(store, Map.of("data/input", payload));
        String name = ".campaign-layers/" + hash + ".zip";
        RemoteCampaignStorage.retainLayers(root, List.of(name)); RemoteCampaignStorage.retainLayers(other, List.of(name));
        RemoteCampaignStorage.install(root, name); RemoteCampaignStorage.install(other, name);
        RemoteCampaignStorage.acknowledge(root, id, receipt); RemoteCampaignStorage.retire(root);
        assertTrue(Files.exists(store.resolve(hash + ".zip"))); assertFalse(Files.exists(root.resolve("data/input")));
        assertEquals("input", Files.readString(other.resolve("data/input")));
        RemoteCampaignStorage.retire(other); assertFalse(Files.exists(store.resolve(hash + ".zip")));
    }
}
