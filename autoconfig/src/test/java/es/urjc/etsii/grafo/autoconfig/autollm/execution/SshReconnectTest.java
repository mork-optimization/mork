package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SshReconnectTest {
    @TempDir Path root;
    @org.junit.jupiter.api.BeforeEach void saveOwner() { saveOwner(root); }
    private void saveOwner(Path directory) {
        var config = config();
        AutoLlmFileUtil.write(directory.resolve("execution-owner.json"), new ExecutionOwner(WorkloadProtocol.VERSION, "fixture", null,
                directory.toAbsolutePath().toString(), WorkloadPathUtil.remoteRoot(config, directory), config, "fixture-controller", "fixture-workspace"));
    }
    private AutoLlmConfig config() {
        var defaults = AutoLlmConfig.defaults();
        var server = new AutoLlmConfig.Server("test", "unused", "/work", "java", Map.of(), null, null, null);
        return new AutoLlmConfig(defaults.project(), root.toString(), 1, null, null, null, null, null, null,
                defaults.codex(), defaults.preparation(), defaults.tuning(), defaults.comparison(),
                new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH,
                        new AutoLlmConfig.Ssh("test", "unused.jar", List.of(server), new AutoLlmConfig.Reconnect(60_000L, 60_000L, 120_000L))));
    }
    private AutoLlmWorkloadExecutor.Workload work() {
        return new AutoLlmWorkloadExecutor.Workload(List.of("unused"), root, root.resolve("output"), Map.of(), List.of(), List.of(), 1000, 0, 100, false, 1000);
    }
    @Test void stagesShareFrozenExecutableLayerInsteadOfRepeatingItInPayloads() throws Exception {
        Path jar = root.resolve("frozen/project.jar"); Files.createDirectories(jar.getParent());
        Files.writeString(jar, "immutable executable");
        var transport = mock(SshTransport.class);
        doThrow(new ExecutionTargetException("fixture stops before remote IO")).when(transport).bootstrap();
        String layer = null;
        for (String stage : List.of("tuning", "comparison")) {
            Path output = root.resolve(stage);
            var work = new AutoLlmWorkloadExecutor.Workload(List.of("unused"), root, output,
                    Map.of("frozen/project.jar", jar), List.of(), List.of(), 1000, 0, 100, false, 1000);
            assertThrows(ExecutionTargetException.class, () -> new SshWorkloadExecutor(config(), root, transport, state -> {}).run(work, () -> false));
            var job = AutoLlmFileUtil.read(output.resolve("remote-job.json"), WorkloadProtocol.Job.class);
            assertEquals(1, job.layers().size());
            if (layer != null) assertEquals(layer, job.layers().getFirst());
            layer = job.layers().getFirst();
            try (var zip = new java.util.zip.ZipFile(root.resolve(".transfer/" + job.id() + ".zip").toFile())) {
                assertNull(zip.getEntry("frozen/project.jar"));
            }
        }
        Path archive = root.resolve(".transfer/layers/" + Path.of(layer).getFileName());
        assertEquals(AutoLlmFileUtil.hash(jar), WorkloadArchiveUtil.verify(archive, root).get("frozen/project.jar").sha256());
    }
    @Test void managedTrialsShareBaselineExecutableTransfersAcrossOwners() throws Exception {
        AutoLlmFileUtil.write(root.resolve("control/storage-layout.json"), Map.of("version", 1));
        Path jar = root.resolve("baseline.jar"); Files.writeString(jar, "same executable for every trial");
        var transport = mock(SshTransport.class);
        doThrow(new ExecutionTargetException("fixture stops before remote IO")).when(transport).bootstrap();
        String previous = null;
        for (String number : List.of("001", "002")) {
            Path trial = root.resolve("trials/" + number), output = trial.resolve("check"); saveOwner(trial);
            String name = "baseline/source/target/project.jar";
            var work = new AutoLlmWorkloadExecutor.Workload(List.of("unused"), trial, output,
                    Map.of(name, jar), List.of(), List.of(), 1000, 0, 100, false, 1000);
            assertThrows(ExecutionTargetException.class, () -> new SshWorkloadExecutor(config(), trial, transport, _ -> {}).run(work, () -> false));
            var job = AutoLlmFileUtil.read(output.resolve("remote-job.json"), WorkloadProtocol.Job.class);
            assertTrue(job.managedStorage()); assertEquals(1, job.layers().size());
            String layer = job.layers().getFirst(); assertTrue(layer.startsWith(".campaign-layers/"));
            if (previous != null) assertEquals(previous, layer);
            previous = layer;
            try (var zip = new java.util.zip.ZipFile(trial.resolve(".transfer/" + job.id() + ".zip").toFile())) { assertNull(zip.getEntry(name)); }
        }
        try (var files = Files.newDirectoryStream(root.resolve(".store/layers"), "*.zip")) {
            int count = 0; for (Path ignored : files) count++; assertEquals(1, count);
        }
    }
    @ParameterizedTest
    @ValueSource(strings = {"offline", "agent refused operation\nPermission denied (publickey)."})
    void restartRetainsTheExhaustedDisconnectionBudgetAndJobIdentity(String diagnostic) {
        var transport = mock(SshTransport.class);
        var work = work();
        AutoLlmFileUtil.write(work.output().resolve("remote-status.json"), Map.of("disconnectedAt", Instant.now().minusSeconds(300).toString(), "consecutiveFailures", 4, "connectionRetries", 4));
        doThrow(SshTransport.connectionFailure(diagnostic)).when(transport).bootstrap();
        var executor = new SshWorkloadExecutor(config(), root, transport, state -> {});
        var failure = assertThrows(AutoLlmFailureUtil.StageException.class, () -> executor.run(work, () -> false));
        assertEquals("REMOTE_UNREACHABLE", failure.failure().code());
        String identity = AutoLlmFileUtil.hash(work.output().resolve("remote-job.json"));
        assertThrows(AutoLlmFailureUtil.StageException.class, () -> new SshWorkloadExecutor(config(), root, transport, state -> {}).run(work, () -> false));
        assertEquals(identity, AutoLlmFileUtil.hash(work.output().resolve("remote-job.json")));
        verify(transport, times(2)).bootstrap();
        verifyNoMoreInteractions(transport);
    }
    @Test void cancellationAttemptsReconnectPromptlyThenRespectsBackoff() throws Exception {
        var transport = mock(SshTransport.class); var cancelled = new AtomicBoolean(); var calls = new AtomicInteger();
        var cancelling = new CountDownLatch(1); var repeated = new CountDownLatch(1);
        doAnswer(_ -> {
            if (calls.incrementAndGet() > 2) repeated.countDown();
            throw new SshTransport.ConnectionException("offline");
        }).when(transport).bootstrap();
        try (var pool = Executors.newSingleThreadExecutor()) {
            var future = pool.submit(() -> new SshWorkloadExecutor(config(), root, transport, state -> {
                if ("WAITING_FOR_REMOTE".equals(state.get("status"))) cancelled.set(true);
                if ("CANCELLATION_PENDING".equals(state.get("status"))) cancelling.countDown();
            }).run(work(), cancelled::get));
            try {
                assertTrue(cancelling.await(3, TimeUnit.SECONDS), "Cancellation must interrupt the minute-long retry delay");
                assertFalse(repeated.await(100, TimeUnit.MILLISECONDS), "An unreachable host must not cause a cancellation retry loop");
                assertEquals(2, calls.get());
            } finally { future.cancel(true); }
        }
    }
}
