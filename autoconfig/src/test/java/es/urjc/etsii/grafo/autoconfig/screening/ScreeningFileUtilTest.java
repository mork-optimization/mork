package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.util.IOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;
import static org.junit.jupiter.api.Assertions.*;

class ScreeningFileUtilTest {
    @TempDir Path temp;
    private String proposal() throws Exception {
        Path source = temp.resolve("source.txt"), second = temp.resolve("second.txt"), sota = temp.resolve("sota.json");
        Files.writeString(source, "input"); Files.writeString(second, "second input"); Files.writeString(sota, "{}");
        var config = new InstanceScreeningConfig(sota.toString(), temp.toString(), null, null, null, null, null, null, null, null, null);
        var manifest = new Manifest(1, "fingerprint", config, 1234, "Xoroshiro128PlusPlus", .15, "Cost", FMode.MINIMIZE,
                ScreeningFileUtil.host(), List.of(new Input(source.toString(), ScreeningFileUtil.inputHash(source.toString())),
                        new Input(second.toString(), ScreeningFileUtil.inputHash(second.toString()))), AutoLlmFileUtil.hash(sota), "jar", List.of());
        AutoLlmFileUtil.write(temp.resolve("manifest.json"), manifest);
        var hashes = new LinkedHashMap<String,String>();
        for (String name : List.of("retained", "train", "test")) {
            Path file = temp.resolve("proposed-" + name + ".index");
            Files.writeString(file, name.equals("train") ? "source.txt\n" : "source.txt\nsecond.txt\n");
            hashes.put(file.getFileName().toString(), AutoLlmFileUtil.hash(file));
        }
        Files.createDirectories(temp.resolve("runs")); Files.writeString(temp.resolve("runs/run.json"), "evidence");
        AutoLlmFileUtil.write(temp.resolve("run-hashes.json"), Map.of("run.json", AutoLlmFileUtil.hash(temp.resolve("runs/run.json"))));
        hashes.put("run-hashes.json", AutoLlmFileUtil.hash(temp.resolve("run-hashes.json")));
        AutoLlmFileUtil.write(temp.resolve("proposal.json"), new Proposal("fingerprint", "PENDING_REVIEW", List.of(), hashes, 1, 2, null));
        return AutoLlmFileUtil.hash(temp.resolve("proposal.json"));
    }
    @Test void onlyExplicitMatchingReviewPublishesIndexes() throws Exception {
        String sha = proposal();
        assertFalse(Files.exists(temp.resolve("train.index")));
        assertThrows(IllegalArgumentException.class, () -> ScreeningFileUtil.approve(temp, null, "jar"));
        assertThrows(IllegalArgumentException.class, () -> ScreeningFileUtil.approve(temp, "different", "jar"));
        assertFalse(Files.exists(temp.resolve("approved.json")));
        ScreeningFileUtil.approve(temp, sha, "jar");
        assertEquals("source.txt\n", Files.readString(temp.resolve("train.index")));
        assertEquals(Files.readString(temp.resolve("retained.index")), Files.readString(temp.resolve("test.index")));
        assertEquals("APPROVED", AutoLlmFileUtil.parseObject(Files.readString(temp.resolve("approved.json"))).get("state"));
    }
    @Test void changedInputsArtifactProposalsAndEvidenceInvalidateApproval() throws Exception {
        String sha = proposal();
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.approve(temp, sha, "different jar"));
        Files.writeString(temp.resolve("source.txt"), "changed");
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.approve(temp, sha, "jar"));
        Files.writeString(temp.resolve("source.txt"), "input");
        Files.writeString(temp.resolve("proposed-train.index"), "changed");
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.approve(temp, sha, "jar"));
        Files.writeString(temp.resolve("proposed-train.index"), "source.txt\n");
        Files.writeString(temp.resolve("runs/run.json"), "changed");
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.approve(temp, sha, "jar"));
        assertFalse(Files.exists(temp.resolve("train.index")));
    }
    @Test void noViableSplitCannotBeApproved() throws Exception {
        proposal();
        Proposal old = AutoLlmFileUtil.read(temp.resolve("proposal.json"), Proposal.class);
        AutoLlmFileUtil.write(temp.resolve("proposal.json"), new Proposal(old.fingerprint(), old.state(), old.instances(), old.files(), 0, 0, "No retained instances"));
        String sha = AutoLlmFileUtil.hash(temp.resolve("proposal.json"));
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.approve(temp, sha, "jar"));
    }
    @Test void testingCannotExcludeTrainingOrAnyOtherRetainedInstance() throws Exception {
        proposal();
        Proposal p = AutoLlmFileUtil.read(temp.resolve("proposal.json"), Proposal.class);
        Files.writeString(temp.resolve("proposed-test.index"), "second.txt\n");
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.validateSelection(temp, p));
        Files.writeString(temp.resolve("proposed-test.index"), "source.txt\n");
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.validateSelection(temp, p));
        Files.writeString(temp.resolve("proposed-test.index"), "source.txt\nsecond.txt\n");
        assertDoesNotThrow(() -> ScreeningFileUtil.validateSelection(temp, p));
    }
    @Test void singleRetainedInstanceCanBeUsedForBothTrainingAndTesting() throws Exception {
        proposal();
        Proposal p = AutoLlmFileUtil.read(temp.resolve("proposal.json"), Proposal.class);
        Files.writeString(temp.resolve("proposed-retained.index"), "source.txt\n");
        Files.writeString(temp.resolve("proposed-test.index"), "source.txt\n");
        var single = new Proposal(p.fingerprint(), p.state(), p.instances(), p.files(), 1, 1, null);
        assertDoesNotThrow(() -> ScreeningFileUtil.validateSelection(temp, single));
    }
    @Test void relativeIndexPreservesArchiveEntryAndSurvivesWorkingDirectoryChanges() throws Exception {
        Path archive = temp.resolve("datasets.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("nested/instance two.txt")); zip.write("payload".getBytes()); zip.closeEntry();
        }
        Path index = temp.resolve("screening/proposed-retained.index");
        ScreeningFileUtil.index(index, List.of(archive + "!nested/instance two.txt"));
        assertEquals("../datasets.zip!nested/instance two.txt\n", Files.readString(index));
        String token = index.getParent().resolve("../datasets.zip").normalize() + "!nested/instance two.txt";
        try (var input = IOUtil.getInputStream(token)) { assertEquals("payload", new String(input.readAllBytes())); }
    }
    @Test void resumeRejectsForeignRecordsAndRetainsTheOriginalRunEvidence() throws Exception {
        proposal(); Manifest manifest = AutoLlmFileUtil.read(temp.resolve("manifest.json"), Manifest.class);
        Input input = manifest.inputs().getFirst();
        var run = new Run(manifest.fingerprint(), input.path(), 1234, false, null, 0, List.of(), "interrupted");
        ScreeningFileUtil.writeRun(temp, run);
        assertEquals(run, ScreeningFileUtil.readRun(temp, manifest, input, 1234));
        Path file = ScreeningFileUtil.runPath(temp, input.path(), 1234);
        Files.writeString(file, Files.readString(file).replace("interrupted", "modified"));
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.readRun(temp, manifest, input, 1234));
        ScreeningFileUtil.writeRun(temp, new Run("other", input.path(), 1234, false, null, 0, List.of(), "different"));
        assertThrows(IllegalStateException.class, () -> ScreeningFileUtil.readRun(temp, manifest, input, 1234));
    }
}
