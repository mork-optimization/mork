package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmEvidenceFilesTest {
    @TempDir Path root;

    @Test void preservesExactBytesOffsetsAndTailsAcrossChunksAndRelocation() throws Exception {
        Path directory = root.resolve("original"), file = directory.resolve("conversation ü.jsonl"); Files.createDirectories(directory);
        String text = "first\n" + "α€ multiline record\n".repeat(160_000) + "last\n";
        Files.writeString(file, text); byte[] original = Files.readAllBytes(file);
        String hash = AutoLlmFileUtil.hash(file); var tail = AutoLlmLogUtil.tail(file, 5);
        var page = AutoLlmLogUtil.read(file, AutoLlmEvidenceFiles.CHUNK_BYTES - 3, 5000);
        AutoLlmEvidenceFiles.compact(file);
        assertFalse(Files.exists(file)); assertTrue(AutoLlmEvidenceFiles.exists(file));
        assertTrue(Files.size(AutoLlmEvidenceFiles.archive(file)) < original.length / 10);
        assertEquals(hash, AutoLlmFileUtil.hash(file)); assertEquals(text, AutoLlmEvidenceFiles.readString(file));
        assertThrows(java.io.UncheckedIOException.class, () -> AutoLlmFileUtil.append(file, Map.of("unexpected", "write")));
        assertFalse(Files.exists(file)); assertEquals(hash, AutoLlmFileUtil.hash(file));
        assertEquals(tail, AutoLlmLogUtil.tail(file, 5)); assertEquals(page, AutoLlmLogUtil.read(file, page.cursor(), 5000));
        assertArrayEquals(Arrays.copyOfRange(original, AutoLlmEvidenceFiles.CHUNK_BYTES - 4, AutoLlmEvidenceFiles.CHUNK_BYTES + 8),
                AutoLlmEvidenceFiles.range(file, AutoLlmEvidenceFiles.CHUNK_BYTES - 4, 12));
        assertEquals(List.of(file), AutoLlmEvidenceFiles.files(directory));
        AutoLlmEvidenceFiles.compact(file); // Already archived: no replacement or unpacking.
        Path relocated = root.resolve("relocated"); Files.move(directory, relocated);
        assertEquals(hash, AutoLlmFileUtil.hash(relocated.resolve(file.getFileName())));
    }

    @Test void corruptChunksPreventDeletionAndNeverReturnUnverifiedBytes() throws Exception {
        Path file = root.resolve("stdout.log"); byte[] original = "important evidence\n".repeat(1000).getBytes(StandardCharsets.UTF_8);
        Files.write(file, original); AutoLlmEvidenceFiles.compact(file);
        Path archive = AutoLlmEvidenceFiles.archive(file), replacement = root.resolve("corrupt.zip");
        try (var input = new ZipFile(archive.toFile()); var output = new ZipOutputStream(Files.newOutputStream(replacement))) {
            var entries = input.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement(); byte[] bytes;
                try (var content = input.getInputStream(entry)) { bytes = content.readAllBytes(); }
                if (entry.getName().equals("chunks/0")) bytes[0] ^= 1;
                output.putNextEntry(new ZipEntry(entry.getName())); output.write(bytes); output.closeEntry();
            }
        }
        Files.move(replacement, archive, StandardCopyOption.REPLACE_EXISTING);
        Files.write(file, original); // Simulate a crash after publication but before removing the loose file.
        assertThrows(IOException.class, () -> AutoLlmEvidenceFiles.compact(file));
        assertArrayEquals(original, Files.readAllBytes(file));
        Files.delete(file);
        assertThrows(IOException.class, () -> AutoLlmEvidenceFiles.range(file, 0, 5));
    }

    @Test void refusesToDiscardAChangedLooseFileAfterInterruptedCompaction() throws Exception {
        Path file = root.resolve("events.csv"); Files.writeString(file, "old evidence\n"); AutoLlmEvidenceFiles.compact(file);
        Files.writeString(file, "new unique evidence\n");
        assertThrows(IOException.class, () -> AutoLlmEvidenceFiles.compact(file));
        assertEquals("new unique evidence\n", Files.readString(file));
    }

    @Test void compactRequiresNewLayoutTerminalTrialAndCollectedJobsAndLeavesSourcesAlone() throws Exception {
        Path campaign = root.resolve("campaign"), trial = campaign.resolve("trials/001"), file = trial.resolve("attempts/001/stdout.log");
        Files.createDirectories(file.getParent()); Files.writeString(file, "closed log\n".repeat(1000));
        Path source = trial.resolve("attempts/001/source/data.csv"); Files.createDirectories(source.getParent()); Files.writeString(source, "source data\n".repeat(1000));
        var state = new AutoLlmCampaign.Trial(); state.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
        AutoLlmEvidenceFiles.compact(campaign, trial, state); assertTrue(Files.exists(file));
        AutoLlmFileUtil.write(campaign.resolve("control/storage-layout.json"), Map.of("version", 1));
        state.status = AutoLlmCampaign.TrialStatus.BLOCKED;
        AutoLlmEvidenceFiles.compact(campaign, trial, state); assertTrue(Files.exists(file));
        state.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
        Path job = file.resolveSibling("remote-job.json"); AutoLlmFileUtil.write(job, Map.of());
        AutoLlmEvidenceFiles.compact(campaign, trial, state); assertTrue(Files.exists(file));
        AutoLlmFileUtil.write(job.resolveSibling("collection-completed.json"), Map.of());
        AutoLlmEvidenceFiles.compact(campaign, trial, state);
        assertFalse(Files.exists(file)); assertTrue(AutoLlmEvidenceFiles.exists(file)); assertTrue(Files.exists(source));
        AutoLlmEvidenceFiles.compact(campaign, trial, state); assertEquals("closed log\n".repeat(1000), AutoLlmEvidenceFiles.readString(file));
    }

    @Test @SuppressWarnings("rawtypes") void comparisonSummariesCurvesAndStreamingRowsAreIdenticalAfterCompression() throws Exception {
        var objective = mock(es.urjc.etsii.grafo.solution.Objective.class);
        when(objective.getName()).thenReturn("cost"); when(objective.getFMode()).thenReturn(es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE);
        es.urjc.etsii.grafo.util.Context.Configurator.setObjectives(objective);
        try {
        Path file = root.resolve("events.jsonl");
        var row = new LinkedHashMap<String,Object>(); row.put("kind", "evaluation-result"); row.put("measured", true); row.put("state", "SUCCEEDED");
        row.put("algorithm", "SOTA"); row.put("instanceId", "one"); row.put("instance", "instance.txt"); row.put("iteration", "0");
        row.put("objectives", Map.of("cost", 12)); row.put("executionTimeNanos", 2000); row.put("timeToBestNanos", 1000);
        row.put("curves", Map.of("cost", List.of(Map.of("instant", 0, "value", 15), Map.of("instant", 1, "value", 12))));
        AutoLlmFileUtil.append(file, row);
        var cache = new AutoLlmComparisonUtil.Cache();
        Object summary = cache.summary(file), curves = cache.curves(file, "one");
        var rows = AutoLlmFileUtil.events(file);
        AutoLlmEvidenceFiles.compact(file);
        assertEquals(AutoLlmFileUtil.canonicalHash(summary), AutoLlmFileUtil.canonicalHash(cache.summary(file)));
        assertEquals(AutoLlmFileUtil.canonicalHash(curves), AutoLlmFileUtil.canonicalHash(cache.curves(file, "one")));
        assertEquals(AutoLlmFileUtil.canonicalHash(summary), AutoLlmFileUtil.canonicalHash(new AutoLlmComparisonUtil.Cache().summary(file)));
        assertEquals(rows, AutoLlmFileUtil.events(file));
        } finally { es.urjc.etsii.grafo.util.Context.reset(); }
    }

    @Test void artifactListingDownloadsAndLogEndpointsKeepLogicalPaths() throws Exception {
        var config = new AutoLlmConfig(null, root.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var store = new AutoLlmCampaignStore(config); String id = UUID.randomUUID().toString();
        Path file = store.directory(id).resolve("trials/001/attempts/001/conversations/thread.jsonl");
        Files.createDirectories(file.getParent()); Files.writeString(file, "{\"text\":\"unique conversation\"}\n".repeat(1000));
        var before = store.artifacts(id, null, true); String text = Files.readString(file);
        AutoLlmEvidenceFiles.compact(file);
        assertEquals(before, store.artifacts(id, null, true));
        String logical = before.getFirst().path(); assertEquals(file, store.artifact(id, logical));
        var controller = new AutoLlmController(mock(AutoLlmCampaignService.class), store, mock(MonitorModeState.class), new MockEnvironment());
        var download = controller.download(id, logical);
        try (var input = Objects.requireNonNull(download.getBody()).getInputStream()) { assertEquals(text, new String(input.readAllBytes(), StandardCharsets.UTF_8)); }
        assertEquals(text.getBytes(StandardCharsets.UTF_8).length, download.getHeaders().getContentLength());
        assertEquals(text, controller.log(id, logical, 0, 65536).text());
        assertEquals(3, controller.preview(id, logical, 3).lineCount());
        String physical = store.directory(id).relativize(AutoLlmEvidenceFiles.archive(file)).toString().replace('\\', '/');
        assertThrows(NoSuchElementException.class, () -> store.artifact(id, physical));
    }
}
