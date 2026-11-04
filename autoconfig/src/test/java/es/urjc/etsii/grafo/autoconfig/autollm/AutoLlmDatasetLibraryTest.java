package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.OwnershipManifest;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.util.InstancePathUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmDatasetLibraryTest {
    @TempDir Path temp;
    @org.junit.jupiter.api.BeforeEach void realPaths() throws Exception { temp = temp.toRealPath(); }
    private AutoLlmConfig config(Path repository, Path training, Path comparison) {
        return new AutoLlmConfig(new AutoLlmConfig.Project(repository.toString(), "HEAD", null, "example/target/app.jar", null),
                repository.resolve("campaigns").toString(), 1, null, null, null, null, training.toString(), comparison.toString(), null, null, null, null, null);
    }
    private Path input(String name) throws Exception { Path path = temp.resolve(name); Files.createDirectories(path.getParent()); return Files.writeString(path, name); }
    private AutoLlmCampaignService service(AutoLlmConfig c, InstanceConfiguration instances) {
        return new AutoLlmCampaignService(c, new AutoLlmCampaignStore(c), mock(AutoLlmProcessRunner.class), mock(CodexTaskRunner.class), mock(AutoLlmReportService.class), instances);
    }

    @Test void publishesExactIndependentOrdersAndCopiesArchiveContainerOnce() throws Exception {
        Path zip = temp.resolve("pool.zip");
        try (var output = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (String name : List.of("a", "b", "c")) { output.putNextEntry(new ZipEntry(name)); output.write(name.getBytes()); output.closeEntry(); }
        }
        Path training = Files.write(temp.resolve("train.index"), List.of("pool.zip!b", "pool.zip!a"));
        Path comparison = Files.write(temp.resolve("test.index"), List.of("pool.zip!a", "pool.zip!c", "pool.zip!b"));
        var library = new AutoLlmDatasetLibrary(config(temp, training, comparison));
        var review = library.preview("Archive selection");
        assertFalse(Files.exists(temp.resolve(".mork")), "Preview is read-only");
        var entry = library.publish(review.name(), review.digest());
        assertEquals(2, entry.overlap()); assertEquals(1, library.list().size());
        assertEquals(entry, library.publish(review.name(), review.digest()), "Approval retries return the same immutable entry");
        var train = InstancePathUtil.list(entry.trainingPath()); var test = InstancePathUtil.list(entry.comparisonPath());
        assertTrue(train.getFirst().endsWith("!b")); assertTrue(train.getLast().endsWith("!a"));
        assertTrue(test.getFirst().endsWith("!a")); assertTrue(test.get(1).endsWith("!c")); assertTrue(test.getLast().endsWith("!b"));
        assertEquals(5, entry.files().size(), "One archive plus the two indexes, aliases and provenance");
        assertEquals(train.getFirst(), AutoLlmInputUtil.aliases(Path.of(entry.trainingPath()), Path.of(entry.comparisonPath())).get(zip + "!b"));
        Files.delete(zip); Files.delete(training); Files.delete(comparison);
        assertEquals(entry, library.get(entry.id()));
        for (String token : test) assertDoesNotThrow(() -> AutoLlmFileUtil.inputHash(token));
    }

    @Test void distinctInstancesWithTheSameNameAndBytesDoNotCollapse() throws Exception {
        Path a = input("first/same.txt"), b = input("second/same.txt"); Files.writeString(b, Files.readString(a));
        Path index = Files.write(temp.resolve("train.index"), List.of(a.toString(), b.toString()));
        var library = new AutoLlmDatasetLibrary(config(temp, index, index)); var review = library.preview("Separate identities");
        var entry = library.publish(review.name(), review.digest());
        var members = InstancePathUtil.list(entry.trainingPath());
        assertEquals(2, new HashSet<>(members).size());
        assertEquals(AutoLlmFileUtil.inputHash(members.getFirst()), AutoLlmFileUtil.inputHash(members.getLast()));
    }

    @Test void historicalScreeningProvenanceAndOriginalAliasesAreRetainedWithoutItsExecutable() throws Exception {
        Path proposal = temp.resolve("screening/proposal"); Files.createDirectories(proposal);
        Path a = input("screening/data/a");
        AutoLlmFileUtil.write(proposal.getParent().resolve("remote-screening.json"), Map.of());
        AutoLlmFileUtil.write(proposal.resolve("proposal.json"), Map.of("instances", List.of(Map.of("instance", "excluded", "reason", "Historical exclusion"))));
        AutoLlmFileUtil.write(proposal.resolve("manifest.json"), Map.of("artifactSha256", "old-executable", "host", "original-host"));
        AutoLlmFileUtil.write(proposal.getParent().resolve("data/original-inputs.json"), Map.of("original-a", Map.of("path", a.toString())));
        Path index = Files.write(proposal.resolve("proposed-train.index"), List.of(a.toString()));
        var library = new AutoLlmDatasetLibrary(config(temp, index, index)); var review = library.preview("Screened");
        assertTrue(AutoLlmFileUtil.JSON.writeValueAsString(review.provenance()).contains("Historical exclusion"));
        var entry = library.publish(review.name(), review.digest());
        assertEquals(InstancePathUtil.list(entry.trainingPath()).getFirst(), AutoLlmInputUtil.aliases(Path.of(entry.trainingPath()), Path.of(entry.comparisonPath())).get("original-a"));
        assertFalse(Files.exists(proposal.getParent().resolve("baseline")));
    }

    @Test void changesBetweenReviewAndApprovalAndAfterPublicationAreRejected() throws Exception {
        Path a = input("a"), b = input("b");
        var library = new AutoLlmDatasetLibrary(config(temp, a, b));
        var review = library.preview("Disjoint");
        assertThrows(IllegalArgumentException.class, () -> library.publish("Disjoint", null));
        Files.writeString(a, "changed");
        assertThrows(IllegalArgumentException.class, () -> library.publish("Disjoint", review.digest()));
        var current = library.preview("Disjoint"); var entry = library.publish("Disjoint", current.digest());
        assertEquals(0, entry.overlap());
        var revisedBaseline = new AutoLlmDatasetLibrary(config(temp, a, b).withRef("different-baseline"));
        assertEquals(entry, revisedBaseline.get(entry.id()));
        Files.writeString(Path.of(InstancePathUtil.list(entry.trainingPath()).getFirst()), "tampered");
        assertThrows(IllegalStateException.class, () -> library.get(entry.id()));
        assertThrows(IllegalStateException.class, () -> AutoLlmDatasetLibrary.verifySelection(Path.of(entry.trainingPath()), Path.of(entry.comparisonPath())));
    }

    @Test void validatesMissingEmptyDuplicateAndBrokenArchiveInputs() throws Exception {
        Path a = input("a"); Path index = Files.writeString(temp.resolve("empty.index"), "# no instances\n");
        assertThrows(IllegalArgumentException.class, () -> new AutoLlmDatasetLibrary(config(temp, index, a)).preview("Empty"));
        assertThrows(IllegalArgumentException.class, () -> new AutoLlmDatasetLibrary(config(temp, a, a).withPaths(a.toString(), null)).preview("Missing"));
        Files.write(index, List.of("a", "a"));
        assertThrows(IllegalArgumentException.class, () -> new AutoLlmDatasetLibrary(config(temp, index, a)).preview("Duplicate"));
        Files.write(index, List.of("missing.zip!entry"));
        assertThrows(RuntimeException.class, () -> new AutoLlmDatasetLibrary(config(temp, index, a)).preview("Broken"));
    }

    @Test void onlyNamedDatasetDefaultsAreInferred() throws Exception {
        Path a = input("a"), b = input("b"); var c = config(temp, a, b).withPaths(null, null);
        var instances = new InstanceConfiguration(); instances.setPath(Map.of("default", a.toString()));
        var service = service(c, instances);
        try {
            assertNull(service.defaults().trainingPath()); assertNull(service.defaults().comparisonPath());
            instances.setPath(Map.of("default", "ignored", "irace", a.toString(), "autollm-comparison", b.toString()));
            assertEquals(a.toString(), service.defaults().trainingPath()); assertEquals(b.toString(), service.defaults().comparisonPath());
        } finally { service.close(); }
    }

    @Test void campaignDatasetEditsAllowTrainingOnlyAndAccountForBothSets() throws Exception {
        Files.createDirectories(temp.resolve(".git")); Path a = input("a"), b = input("b"), c = input("c");
        Path training = Files.write(temp.resolve("train.index"), List.of(a.toString(), c.toString()));
        Path sota = Files.writeString(temp.resolve("sota.json"), "{\"$component\":\"Dummy\"}");
        var raw = config(temp, training, b);
        var settings = new AutoLlmConfig(raw.project(), raw.workspace(), 1, null, null, sota.toString(), null, raw.trainingPath(), raw.comparisonPath(), null, null, null, null, null);
        var service = service(settings, new InstanceConfiguration());
        try {
            var draft = service.create(settings);
            var revised = service.reviseDataset(draft.id, new AutoLlmCampaignService.DatasetSelection(List.of(c.toString()), List.of(b.toString()), Map.of(a.toString(), "Excluded by review")));
            assertEquals(List.of(c.toString()), InstancePathUtil.list(revised.config.trainingPath()));
            assertEquals(List.of(b.toString()), InstancePathUtil.list(revised.config.comparisonPath()));
            assertNull(revised.approvedProposal);
            var library = new AutoLlmDatasetLibrary(revised.config);
            var review = library.preview("Edited selection");
            assertTrue(AutoLlmFileUtil.JSON.writeValueAsString(review.provenance()).contains("Excluded by review"));
            var saved = library.publish(review.name(), review.digest());
            assertEquals(0, saved.overlap());
            assertThrows(IllegalArgumentException.class, () -> service.reviseDataset(draft.id, new AutoLlmCampaignService.DatasetSelection(List.of(a.toString()), List.of(b.toString()), Map.of())));
            assertThrows(IllegalArgumentException.class, () -> service.reviseDataset(draft.id, new AutoLlmCampaignService.DatasetSelection(List.of(a.toString()), List.of(b.toString()), Map.of(a.toString(), "Conflict", c.toString(), "Excluded"))));
        } finally { service.close(); }
    }

    @Test void publishedDataSurvivesCleanupOfItsOriginalCampaignAndNewWorkspaces() throws Exception {
        Path repository = temp.toRealPath(); String id = UUID.randomUUID().toString();
        Path a = input("campaigns/" + id + "/a"), b = input("campaigns/" + id + "/b");
        var c = config(repository, a, b); var store = new AutoLlmCampaignStore(c);
        var ownership = new OwnershipManifest(store.root()); ownership.directory(store.root(), "workspace", store.root().toString());
        ownership.directory(store.directory(id), "campaign", store.directory(id).toString());
        var library = new AutoLlmDatasetLibrary(c); var review = library.preview("Durable"); var entry = library.publish(review.name(), review.digest());
        var cleanup = new AutoLlmCleanup(store); var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertTrue(plan.blockers().isEmpty(), plan.blockers().toString());
        assertEquals("SUCCEEDED", cleanup.apply(plan.digest(), true).status()); assertFalse(Files.exists(a));
        var future = new AutoLlmConfig(c.project(), repository.resolve("new-workspace").toString(), null, null, null, null, null, entry.trainingPath(), entry.comparisonPath(), null, null, null, null, null);
        assertEquals(entry, new AutoLlmDatasetLibrary(future).get(entry.id()));
        assertDoesNotThrow(() -> new AutoLlmDatasetLibrary(future).preview("Reused"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "source"})
    void cleanupRetainsLibrariesEvenWhenWorkspaceContainsTheRepository(String folder) throws Exception {
        Path repository = temp.toRealPath().resolve(folder), a = input("a"); Files.createDirectories(repository);
        var c = config(repository, a, a); var library = new AutoLlmDatasetLibrary(c);
        var review = library.preview("Retain"); var entry = library.publish(review.name(), review.digest());
        var workspaceConfig = new AutoLlmConfig(c.project(), temp.toRealPath().toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(workspaceConfig);
        new OwnershipManifest(store.root()).directory(store.root(), "workspace", store.root().toString());
        var plan = new AutoLlmCleanup(store).plan(new AutoLlmCleanup.Selection("workspace", null, null, false, true));
        assertTrue(plan.retain().containsKey(repository.resolve(".mork/datasets").toString()));
        for (var resource : plan.delete()) assertFalse(Path.of(entry.trainingPath()).startsWith(Path.of(resource.path())));
    }
}
