package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AutoLlmMetadataShutdownTest {
    @TempDir Path root;
    @Test void shutdownTracksAnApiMetadataCheckAndWaitsForItsProcessReceipt() throws Exception {
        root = root.toRealPath(); Files.createDirectories(root.resolve("source"));
        var config = new AutoLlmConfig(new AutoLlmConfig.Project(null, null, List.of("sh", "-c", "sleep 30 & wait"), null, null),
                root.toString(), null, null, null, null, null, null, null, null, null, null, null, null);
        var context = new AutoLlmExecutionContext("campaign", "", root, null); context.config = config;
        context.trial = new AutoLlmCampaign.Trial(); context.trial.number = 1;
        var runner = new AutoLlmTrialRunner(context, root, new AutoLlmProcessRunner(), mock(GenerationTaskRunner.class), new AutoLlmTrialRunner.Listener() {
            public void state(AutoLlmExecutionContext.Snapshot snapshot) {}
            public void event(String kind, Object detail) {}
        }, () -> false, new Semaphore(1));
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            var check = threads.submit(() -> runner.metadataCheck("campaign", 1, null));
            try {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (AutoLlmFileUtil.ownedFiles(root.resolve("metadata"), "owner.json").isEmpty() && !check.isDone() && System.nanoTime() < deadline) Thread.sleep(10);
                assertFalse(AutoLlmFileUtil.ownedFiles(root.resolve("metadata"), "owner.json").isEmpty());
                runner.shutdown();
                assertTrue(runner.awaitShutdown(System.nanoTime() + TimeUnit.SECONDS.toNanos(10)));
                var failure = assertThrows(ExecutionException.class, () -> check.get(2, TimeUnit.SECONDS)).getCause();
                assertInstanceOf(CancellationException.class, failure, es.urjc.etsii.grafo.util.ExceptionUtil.stacktrace(failure));
                assertFalse(AutoLlmFileUtil.ownedFiles(root.resolve("metadata"), "process-terminated.json").isEmpty());
                assertFalse(AutoLlmLocalRecovery.hasUnconfirmed(root));
                assertThrows(CancellationException.class, () -> runner.metadataCheck("campaign", 1, null));
            } finally { runner.shutdown(); check.cancel(true); }
        }
    }
}
