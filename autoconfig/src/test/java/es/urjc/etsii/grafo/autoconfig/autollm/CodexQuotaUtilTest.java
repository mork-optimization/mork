package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodexQuotaUtilTest {
    @TempDir Path temp;
    private static final Instant STARTED = Instant.parse("2026-10-24T14:00:00Z");
    private static final Instant NOW = STARTED.plusSeconds(60);
    private static final String MESSAGE = "You’ve hit your usage limit.";

    private void error(String message) {
        AutoLlmFileUtil.append(temp.resolve("stdout.log"), Map.of("type", "turn.failed", "error", Map.of("message", message)));
    }
    private CodexQuotaUtil.QuotaLimitException detect() {
        return CodexQuotaUtil.detect(temp, STARTED, NOW, ZoneId.of("Europe/Madrid"));
    }
    private Map<String, Object> window(double used, Instant reset) {
        return Map.of("used_percent", used, "resets_at", reset.getEpochSecond());
    }
    private void limits(Map<String, Object> limits) {
        AutoLlmFileUtil.write(temp.resolve("evidence.json"), Map.of("threadId", "saved-thread", "conversations",
                List.of(Map.of("threadId", "saved-thread", "path", "native.jsonl"))));
        AutoLlmFileUtil.append(temp.resolve("native.jsonl"), Map.of("type", "event_msg", "payload",
                Map.of("type", "token_count", "rate_limits", limits)));
    }
    @Test void usesTheExhaustedWindowFromTheArchivedConversation() {
        error(MESSAGE);
        var reset = NOW.plusSeconds(3600);
        limits(Map.of("limit_id", "codex", "primary", window(100, reset), "secondary", window(37, NOW.plusSeconds(604800))));
        limits(Map.of("limit_id", "premium"));
        assertEquals(reset.plusSeconds(2), detect().retryAt());
    }
    @Test void waitsForAllExhaustedWindows() {
        error(MESSAGE);
        var later = NOW.plusSeconds(7200);
        limits(Map.of("primary", window(100, NOW.plusSeconds(3600)), "secondary", window(100, later)));
        assertEquals(later.plusSeconds(2), detect().retryAt());
    }
    @Test void ignoresOldOrNoLongerExhaustedSnapshots() {
        error(MESSAGE);
        limits(Map.of("primary", window(100, NOW.plusSeconds(3600))));
        limits(Map.of("primary", window(50, NOW.plusSeconds(3600)), "secondary", window(100, NOW.minusSeconds(60))));
        assertEquals(NOW.plusSeconds(300), detect().retryAt());
    }
    @Test void fallsBackToTheDisplayedLocalResetTime() {
        error(MESSAGE + " Please try again at 6:31 PM.");
        assertEquals(Instant.parse("2026-10-24T16:31:02Z"), detect().retryAt());
    }
    @Test void handlesResetTimesAfterMidnight() {
        error(MESSAGE + " Please try again at 12:05 AM.");
        var started = Instant.parse("2026-10-24T21:00:00Z");
        var limit = CodexQuotaUtil.detect(temp, started, started.plusSeconds(60), ZoneId.of("Europe/Madrid"));
        assertEquals(Instant.parse("2026-10-24T22:05:02Z"), limit.retryAt());
    }
    @Test void retriesPeriodicallyWhenNoResetTimeIsAvailable() {
        error(MESSAGE);
        assertEquals(NOW.plusSeconds(300), detect().retryAt());
    }
    @Test void recognizesStructuredUsageLimitErrors() {
        AutoLlmFileUtil.append(temp.resolve("stdout.log"), Map.of("type", "turn.failed", "error", Map.of("code", "usage_limit_reached")));
        assertNotNull(detect());
    }
    @ParameterizedTest @ValueSource(strings = {"workspace routing discovery failed", "Authentication failed", "rate_limit_exceeded", "insufficient_quota"})
    void ordinaryFailuresStillRequireTheirExistingRecovery(String message) {
        error(message);
        assertNull(detect());
    }
    @Test void ignoresQuotaTextInAgentMessagesAndSuccessfulTurns() {
        AutoLlmFileUtil.append(temp.resolve("stdout.log"), Map.of("type", "item.completed", "item", Map.of("type", "agent_message", "text", MESSAGE)));
        assertNull(detect());
        error(MESSAGE);
        AutoLlmFileUtil.append(temp.resolve("stdout.log"), Map.of("type", "turn.completed"));
        assertNull(detect());
    }
    @Test void expiredResetTimesDoNotCauseAnImmediateRetryLoop() {
        error(MESSAGE + " Please try again at 4:00 PM.");
        limits(Map.of("primary", window(100, NOW.minusSeconds(1))));
        assertEquals(NOW.plusSeconds(300), detect().retryAt());
        var afterReset = CodexQuotaUtil.detect(temp, STARTED.plusSeconds(5), NOW, ZoneId.of("Europe/Madrid"));
        assertEquals(NOW.plusSeconds(300), afterReset.retryAt());
    }
}
