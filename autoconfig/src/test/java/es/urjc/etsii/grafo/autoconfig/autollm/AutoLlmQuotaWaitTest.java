package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoLlmQuotaWaitTest {
    @TempDir Path temp;

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void restoresTheWaitFromCheckpointAndCanBeCancelled(boolean interrupt) throws Exception {
        var original = new AutoLlmCampaign(); original.id = "quota-fixture";
        original.config = new AutoLlmConfig(null, temp.toString(), 1, null, null, null, null, null, null, null, null, null, null, null);
        var trial = new AutoLlmCampaign.Trial(); trial.number = 1; trial.status = "INTERRUPTED";
        trial.threadId = "saved-conversation"; trial.resumeTurn = true; trial.repairPending = true;
        trial.quotaRetryAt = Instant.now().plusSeconds(86400).toString();
        var attempt = new AutoLlmCampaign.Attempt(); attempt.stage = "REPAIR"; attempt.status = "QUOTA_LIMIT";
        attempt.directory = "trials/001/attempts/001-repair"; trial.attempts.add(attempt); original.trials.add(trial);
        Path checkpoint = temp.resolve("checkpoint.json");
        AutoLlmFileUtil.write(checkpoint, original);
        var restored = AutoLlmFileUtil.read(checkpoint, AutoLlmCampaign.class);
        var codex = mock(CodexTaskRunner.class);
        when(codex.archive(any(), any(), eq(trial.threadId))).thenReturn(
                new CodexEvidenceUtil.Evidence("ARCHIVED", trial.threadId, List.of(), null, null));
        var permits = new Semaphore(1); var cancel = new AtomicBoolean(); var waiting = new CountDownLatch(1);
        var context = new AutoLlmExecutionContext(restored.id, "", temp, restored.prompt);
        context.config = restored.config; context.trial = restored.trials.getFirst();
        var runner = new AutoLlmTrialRunner(context, temp, mock(AutoLlmProcessRunner.class), codex,
                new AutoLlmTrialRunner.Listener() {
                    public void state(AutoLlmExecutionContext.Snapshot snapshot) {
                        restored.trials.set(0, snapshot.trial()); AutoLlmFileUtil.write(checkpoint, restored);
                        if (snapshot.trial().status.equals("WAITING_FOR_QUOTA")) waiting.countDown();
                    }
                    public void event(String kind, Object detail) { }
                }, cancel::get, permits);
        var task = new FutureTask<Void>(() -> { runner.execute(); return null; });
        Thread worker = Thread.ofVirtual().start(task);
        try {
            assertTrue(waiting.await(10, TimeUnit.SECONDS));
            var saved = AutoLlmFileUtil.read(checkpoint, AutoLlmCampaign.class).trials.getFirst();
            assertEquals("WAITING_FOR_QUOTA", saved.status); assertEquals(trial.quotaRetryAt, saved.quotaRetryAt);
            assertEquals(trial.threadId, saved.threadId); assertTrue(saved.repairPending);
            assertEquals(1, permits.availablePermits());
            if (interrupt) worker.interrupt(); else cancel.set(true);
            task.get(5, TimeUnit.SECONDS);
            saved = runner.snapshot().trial();
            assertEquals("INTERRUPTED", saved.status); assertEquals(trial.quotaRetryAt, saved.quotaRetryAt);
            assertTrue(saved.repairPending); assertEquals(0, saved.repairTurns); assertEquals(0, saved.infrastructureRetries);
            verify(codex, never()).run(any(), any(), any(), any(), any(), any());
        } finally {
            cancel.set(true); worker.interrupt(); task.get(5, TimeUnit.SECONDS);
        }
    }
}
