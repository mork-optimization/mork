package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadPathUtil;
import es.urjc.etsii.grafo.util.InstancePathUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.core.type.TypeReference;

import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

class AutoLlmInputTest {
    @TempDir Path temp;

    private Path archive(String payload) throws Exception {
        Path archive = temp.resolve("original instances.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("nested/instance one.txt"));
            zip.write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)); zip.closeEntry();
        }
        return archive;
    }

    @Test void campaignAndWorkersKeepOnlyIndexesAndHashesForTheOriginalDataset() throws Exception {
        Path archive = archive("archived input");
        Path loose = temp.resolve("loose.txt"); Files.writeString(loose, "loose input");
        List<String> inputs = List.of(archive + "!nested/instance one.txt", loose.toString());
        Path campaign = temp.resolve("campaign");
        String canonical = AutoLlmInputUtil.snapshot(campaign, "training", inputs);
        for (String worker : List.of("one", "two")) {
            Path root = campaign.resolve("targets/" + worker);
            String index = AutoLlmInputUtil.snapshot(root, "training", InstancePathUtil.list(canonical));
            assertEquals(inputs, InstancePathUtil.list(index));
            assertFalse(Files.exists(root.resolve("data/training")));
            try (var files = Files.walk(root.resolve("data"))) { assertEquals(2, files.filter(Files::isRegularFile).count()); }
        }
        assertFalse(Files.exists(campaign.resolve("data/training")));
        assertEquals("loose input", Files.readString(loose));
    }

    @Test void sharedOriginalsAndMembershipRemainProtectedByFrozenHashes() throws Exception {
        Path archive = archive("original");
        Path loose = temp.resolve("loose.txt"); Files.writeString(loose, "loose");
        Path root = temp.resolve("campaign");
        String index = AutoLlmInputUtil.snapshot(root, "training", List.of(archive + "!nested/instance one.txt", loose.toString()));
        var hashes = new TreeMap<String,String>(); AutoLlmInputUtil.addHashes(hashes, root, "training");
        AutoLlmFileUtil.verifySources(root, hashes);
        Files.writeString(Path.of(index), loose + "\n");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(root, hashes));
        AutoLlmInputUtil.snapshot(root, "training", List.of(archive + "!nested/instance one.txt", loose.toString()));
        AutoLlmFileUtil.verifySources(root, hashes);
        archive("modified");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(root, hashes));
        archive("original"); Files.delete(loose);
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(root, hashes));
        Files.writeString(loose, "loose");
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) { /* Remove the selected archive member. */ }
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(root, hashes));
    }

    @Test void sshStagingPreservesArchiveMembersAndDistinctEqualNamedInputsWithoutLocalCopies() throws Exception {
        Path archive = archive("archived input");
        Path one = temp.resolve("one/same.txt"), two = temp.resolve("two/same.txt");
        Files.createDirectories(one.getParent()); Files.createDirectories(two.getParent());
        Files.writeString(one, "identical"); Files.writeString(two, "identical");
        List<String> inputs = List.of(archive + "!nested/instance one.txt", one.toString(), two.toString());
        Path root = temp.resolve("worker");
        String index = AutoLlmInputUtil.snapshot(root, "training", inputs);
        var defaults = AutoLlmConfig.defaults();
        var execution = new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH,
                new AutoLlmConfig.Ssh("server", null, List.of(new AutoLlmConfig.Server("server", "server", "/remote", "java", null, null, null, null))));
        var config = new AutoLlmConfig(defaults.project(), defaults.workspace(), null, null, null, null, null, index, index, null, null, null, null, execution);
        config = new AutoLlmExecutionService(new AutoLlmProcessRunner()).stageInputs(config, root);
        assertEquals(inputs, InstancePathUtil.list(config.trainingPath()));
        assertFalse(Files.exists(root.resolve("data/inputs")));
        assertFalse(Files.exists(root.resolve("data/staged")));
        Map<String,Map<String,String>> staged = AutoLlmFileUtil.read(root.resolve("data/staged-inputs.json"), new TypeReference<>() {});
        assertEquals(3, staged.size());
        assertTrue(staged.get(inputs.getFirst()).get("path").endsWith("original instances.zip!nested/instance one.txt"));
        assertNotEquals(staged.get(one.toString()).get("path"), staged.get(two.toString()).get("path"));
        String remote = WorkloadPathUtil.remoteRoot(config, root);
        var localPaths = AutoLlmExecutionService.localPaths(config, root);
        var remoteTokens = new ArrayList<String>();
        for (String token : inputs) {
            String translated = WorkloadPathUtil.translate(staged.get(token).get("path"), root.toString(), remote);
            remoteTokens.add(translated);
            assertEquals(token, localPaths.apply(translated));
        }
        Path metadata = root.resolve("check.json");
        AutoLlmFileUtil.write(metadata, new AutoLlmCheckOrchestrator.Check(1, List.of(), remoteTokens, remoteTokens));
        assertEquals(inputs, AutoLlmExecutionService.check(config, root, metadata).training());
        var frozen = new TreeMap<String,String>(); AutoLlmInputUtil.addHashes(frozen, root, "training");
        AutoLlmFileUtil.verifySources(root, frozen);
        Files.writeString(root.resolve("data/staged-inputs.json"), "{}");
        assertThrows(IllegalStateException.class, () -> AutoLlmFileUtil.verifySources(root, frozen));
    }
}
