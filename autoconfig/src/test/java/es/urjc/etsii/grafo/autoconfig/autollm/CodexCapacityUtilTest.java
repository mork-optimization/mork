package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CodexCapacityUtilTest {
    @TempDir Path root;
    private final Instant start = Instant.parse("2026-11-01T00:00:00Z");
    private Clock clock(Instant instant) { return Clock.fixed(instant, ZoneOffset.UTC); }
    @Test void identifiesOnlyActualCapacityErrors() {
        for (String message : new String[]{"Authentication failed", "insufficient_quota", "rate_limit_exceeded"}) {
            AutoLlmFileUtil.append(root.resolve("stdout.log"), Map.of("type", "turn.failed", "error", Map.of("message", message)));
            assertNull(CodexCapacityUtil.detect(root));
        }
        AutoLlmFileUtil.append(root.resolve("stdout.log"), Map.of("type", "item.completed", "item", Map.of("text", "Selected model is at capacity")));
        assertNull(CodexCapacityUtil.detect(root));
        AutoLlmFileUtil.append(root.resolve("stdout.log"), Map.of("type", "turn.failed", "error", Map.of("code", "model_at_capacity")));
        assertNotNull(CodexCapacityUtil.detect(root));
        AutoLlmFileUtil.append(root.resolve("stdout.log"), Map.of("type", "turn.completed"));
        assertNull(CodexCapacityUtil.detect(root));
        AutoLlmFileUtil.append(root.resolve("stdout.log"), Map.of("type", "error", "message", "Selected model is at capacity. Try again."));
        assertNotNull(CodexCapacityUtil.detect(root));
    }
    @Test void backoffIsBoundedAndCheckpointDoesNotResetDeadline() {
        var wait = CodexCapacityUtil.schedule(null, "capacity", clock(start), () -> 0);
        assertEquals(start.plusSeconds(15).toString(), wait.nextRetryAt());
        Instant now = start;
        for (int i = 1; i < 12; i++) {
            now = now.plusSeconds(1);
            wait = CodexCapacityUtil.schedule(wait, "capacity", clock(now), () -> 0.9999);
            long cap = Math.min(300, 30L << Math.min(i, 4));
            long millis = Duration.between(now, Instant.parse(wait.nextRetryAt())).toMillis();
            assertTrue(millis >= cap * 500 && millis < cap * 1000);
        }
        AutoLlmFileUtil.write(root.resolve("wait.json"), wait);
        var restored = AutoLlmFileUtil.read(root.resolve("wait.json"), CodexCapacityUtil.Wait.class);
        assertEquals(start.plusSeconds(3600).toString(), restored.deadline());
        assertEquals("CODEX_CAPACITY_EXHAUSTED", assertThrows(AutoLlmFailureUtil.StageException.class,
                () -> CodexCapacityUtil.schedule(restored, "capacity", clock(start.plusSeconds(3600)), () -> 0)).failure().code());
    }
    @Test void explicitRecoveryResetsOnlyExhaustedBudget() {
        var trial = new AutoLlmCampaign.Trial(); trial.status = AutoLlmCampaign.TrialStatus.BLOCKED;
        trial.capacityWait = CodexCapacityUtil.schedule(null, "capacity", clock(start), () -> 0);
        trial.threadId = "saved"; trial.repairPending = true;
        trial.queueForResume(); assertNotNull(trial.capacityWait);
        trial.problem = AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "CODEX_CAPACITY_EXHAUSTED", "expired", null).failure();
        trial.queueForResume(); assertNull(trial.capacityWait); assertEquals("saved", trial.threadId); assertTrue(trial.repairPending);
    }
    @Test void recordsModelAndProviderFromSessionEvidence() throws Exception {
        Path transcript = root.resolve("session.jsonl");
        AutoLlmFileUtil.append(transcript, Map.of("type", "session_meta", "payload", Map.of("model_provider", "provider-a")));
        AutoLlmFileUtil.append(transcript, Map.of("type", "turn_context", "payload", Map.of("model", "model-a")));
        assertEquals(new CodexEvidenceUtil.Selection("model-a", "provider-a"), CodexEvidenceUtil.selection(transcript));
    }
}
