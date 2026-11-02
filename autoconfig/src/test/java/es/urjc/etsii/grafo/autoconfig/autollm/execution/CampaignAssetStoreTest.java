package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CampaignAssetStoreTest {
    @TempDir Path root;
    @Test void sharedExecutablesSurviveUntilEveryOwnerReleasesThem() throws Exception {
        Path source = root.resolve("built.jar"); Files.writeString(source, "executable");
        Path first = root.resolve("trials/001"), second = root.resolve("trials/002");
        Path asset = CampaignAssetStore.putArtifact(root, first, source);
        assertEquals(asset, CampaignAssetStore.putArtifact(root, second, source));
        AutoLlmFileUtil.write(first.resolve("frozen/artifact.json"), Map.of("asset", root.relativize(asset).toString().replace('\\', '/')));
        assertEquals(asset, CampaignAssetStore.artifact(root, first));
        CampaignAssetStore.releaseArtifact(root, first); assertTrue(Files.exists(asset));
        CampaignAssetStore.releaseArtifact(root, second); assertFalse(Files.exists(asset));
        CampaignAssetStore.releaseArtifact(root, second);
    }
    @Test void layersAreSharedAcrossTrialsAndRemainReferencedByPendingJobs() throws Exception {
        Path source = root.resolve("input.jar"); Files.writeString(source, "identical bytes");
        Path first = root.resolve("trials/001/attempts/tuning"), second = root.resolve("trials/002/attempts/tuning");
        String hash = CampaignAssetStore.layer(root, first, Map.of("frozen/project.jar", source));
        Path archive = root.resolve(".store/layers/" + hash + ".zip"); var timestamp = Files.getLastModifiedTime(archive);
        assertEquals(hash, CampaignAssetStore.layer(root, second, Map.of("frozen/project.jar", source)));
        assertEquals(timestamp, Files.getLastModifiedTime(archive));
        CampaignAssetStore.releaseLayers(root, root.resolve("trials/001")); assertTrue(Files.exists(archive));
        CampaignAssetStore.releaseLayers(root, root.resolve("trials/002")); assertFalse(Files.exists(archive));
        assertEquals(hash, CampaignAssetStore.layer(root, first, Map.of("frozen/project.jar", source)), "Released copies can be recreated without changing identity");
        Files.writeString(archive, "corrupt");
        assertThrows(java.io.IOException.class, () -> CampaignAssetStore.layer(root, second, Map.of("frozen/project.jar", source)));
    }
    @Test void reinstallChecksContentWhenAnOlderLayerReusesTheSameLogicalPath() throws Exception {
        Path source = root.resolve("input"), cache = root.resolve("layers"), destination = root.resolve("remote");
        Files.writeString(source, "first"); String first = WorkloadArchiveUtil.prepareLayer(cache, Map.of("frozen/project.jar", source));
        Files.writeString(source, "second"); String second = WorkloadArchiveUtil.prepareLayer(cache, Map.of("frozen/project.jar", source));
        WorkloadArchiveUtil.installLayer(cache.resolve(first + ".zip"), destination);
        WorkloadArchiveUtil.installLayer(cache.resolve(second + ".zip"), destination);
        WorkloadArchiveUtil.installLayer(cache.resolve(first + ".zip"), destination);
        assertEquals("first", Files.readString(destination.resolve("frozen/project.jar")));
    }
}
