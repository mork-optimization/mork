package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkloadRecoveryTest {
    @TempDir Path temp;

    @Test void collectionFailureDoesNotRepeatCompletedLocalWork() throws Exception {
        var processes = mock(AutoLlmProcessRunner.class);
        when(processes.run(any(), any(), any(), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyLong()))
                .thenReturn(new AutoLlmProcessRunner.Result(0, false, 1));
        Path output = temp.resolve("output"); Files.createDirectories(output);
        Files.createDirectory(output.resolve("collection-completed.json.tmp"));
        var work = new AutoLlmWorkloadExecutor.Workload(List.of("fixture"), temp, output, Map.of(), List.of(), List.of(), 1000, 0, 100, false, 100);
        var executor = new LocalWorkloadExecutor(processes);
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> executor.run(work, () -> false));
        assertEquals("EVIDENCE_COLLECTION", failure.failure().code());
        Files.delete(output.resolve("collection-completed.json.tmp"));
        assertEquals(0, executor.run(work, () -> false).exitCode());
        assertFalse(Files.exists(output.resolve("collection-failure.json")));
        assertEquals(1, mockingDetails(processes).getInvocations().size());
    }

    @Test void newInvocationCanReplaceCollectedWorkButCannotOverwritePendingEvidence() throws Exception {
        var processes = mock(AutoLlmProcessRunner.class);
        when(processes.run(any(), any(), any(), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyLong()))
                .thenReturn(new AutoLlmProcessRunner.Result(0, false, 1));
        var executor = new LocalWorkloadExecutor(processes);
        Path output = temp.resolve("output");
        var first = new AutoLlmWorkloadExecutor.Workload(List.of("metadata-only"), temp, output, Map.of(), List.of(), List.of(), 1000, 0, 100, false, 100);
        var second = new AutoLlmWorkloadExecutor.Workload(List.of("full-check"), temp, output, Map.of(), List.of(), List.of(), 1000, 0, 100, false, 100);
        executor.run(first, () -> false);
        executor.run(second, () -> false);
        assertEquals(2, mockingDetails(processes).getInvocations().size());
        Files.delete(output.resolve("collection-completed.json"));
        assertThrows(IllegalStateException.class, () -> executor.run(first, () -> false));
        assertEquals(2, mockingDetails(processes).getInvocations().size());
        assertEquals(second.command(), AutoLlmFileUtil.read(output.resolve("workload-result.json"), LocalWorkloadExecutor.Completed.class).command());
    }

    @Test void collectedProcessFailureCanBeRetriedAfterItsCauseIsFixed() {
        var processes = mock(AutoLlmProcessRunner.class);
        when(processes.run(any(), any(), any(), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyLong()))
                .thenReturn(new AutoLlmProcessRunner.Result(1, false, 1), new AutoLlmProcessRunner.Result(0, false, 1));
        var work = new AutoLlmWorkloadExecutor.Workload(List.of("build"), temp, temp.resolve("output"), Map.of(), List.of(), List.of(), 1000, 0, 100, false, 100);
        var executor = new LocalWorkloadExecutor(processes);
        assertEquals(1, executor.run(work, () -> false).exitCode());
        assertEquals(0, executor.run(work, () -> false).exitCode());
        assertEquals(2, mockingDetails(processes).getInvocations().size());
    }

    @Test void unchangedLayersAreNeitherRepackedNorReinstalled() throws Exception {
        Path source = temp.resolve("input"), cache = temp.resolve("cache"), remote = temp.resolve("remote");
        Files.writeString(source, "payload");
        String hash = WorkloadArchiveUtil.prepareLayer(cache, Map.of("data/inputs/a", source));
        Path archive = cache.resolve(hash + ".zip"); var modified = Files.getLastModifiedTime(archive);
        assertEquals(hash, WorkloadArchiveUtil.prepareLayer(cache, Map.of("data/inputs/a", source)));
        assertEquals(modified, Files.getLastModifiedTime(archive));
        WorkloadArchiveUtil.installLayer(archive, remote);
        Path installed = remote.resolve("data/inputs/a"); var installedTime = Files.getLastModifiedTime(installed);
        WorkloadArchiveUtil.installLayer(archive, remote);
        assertEquals(installedTime, Files.getLastModifiedTime(installed));
        assertEquals("payload", Files.readString(installed));
        Files.writeString(source, "new payload");
        assertNotEquals(hash, WorkloadArchiveUtil.prepareLayer(cache, Map.of("data/inputs/a", source)));
    }
}
