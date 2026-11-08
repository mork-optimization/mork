package es.urjc.etsii.grafo.autoconfig.autollm;

import tools.jackson.databind.JsonNode;
import java.io.*;
import java.nio.file.Path;
import java.time.*;
import java.util.Locale;
import java.util.function.DoubleSupplier;

/** Temporary model availability is independent of quota, component repairs and transport retries. */
public final class GenerationCapacityUtil {
    private GenerationCapacityUtil() {}
    public record Wait(String firstFailureAt, String deadline, String nextRetryAt, int retries, String diagnostic) {}
    public static final class CapacityException extends IllegalStateException {
        public CapacityException(String message) { super(message); }
    }
    public static CapacityException detect(Path output) {
        JsonNode error = null;
        Path log = output.resolve("stdout.log");
        if (!AutoLlmEvidenceFiles.exists(log)) return null;
        try (var reader = AutoLlmEvidenceFiles.reader(log)) {
            for (String line; (line = reader.readLine()) != null;) {
                JsonNode event;
                try { event = AutoLlmFileUtil.JSON.readTree(line); } catch (RuntimeException ignored) { continue; }
                switch (event.path("type").asString("")) {
                    case "turn.started", "turn.completed" -> error = null;
                    case "error" -> error = event;
                    case "turn.failed" -> error = event.path("error");
                    default -> { }
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        if (error == null) return null;
        String message = error.path("message").asString("");
        String code = error.path("code").asString("");
        if (!code.equals("model_at_capacity") && !code.equals("model_capacity_exceeded")
                && !error.path("codex_error_info").asString("").equals("ModelAtCapacity")
                && !message.toLowerCase(Locale.ROOT).contains("selected model is at capacity")) return null;
        return new CapacityException(message.isBlank() ? "Selected model is at capacity" : message);
    }
    public static Wait schedule(Wait previous, String diagnostic, Clock clock, DoubleSupplier random) {
        Instant now = clock.instant();
        Instant first = previous == null ? now : Instant.parse(previous.firstFailureAt());
        Instant deadline = previous == null ? first.plusSeconds(3600) : Instant.parse(previous.deadline());
        requireBudget(deadline, now);
        int retries = previous == null ? 1 : previous.retries() + 1;
        long cap = 30_000L << Math.min(retries - 1, 4);
        cap = Math.min(300_000L, cap);
        double jitter = random.getAsDouble();
        if (jitter < 0 || jitter >= 1) throw new IllegalArgumentException("Jitter must be in [0,1)");
        long delay = cap / 2 + (long)(jitter * (cap / 2));
        Instant next = now.plusMillis(delay);
        if (next.isAfter(deadline)) next = deadline;
        return new Wait(first.toString(), deadline.toString(), next.toString(), retries, diagnostic);
    }
    public static void requireBudget(Wait wait, Clock clock) {
        requireBudget(Instant.parse(wait.deadline()), clock.instant());
    }
    private static void requireBudget(Instant deadline, Instant now) {
        if (!now.isBefore(deadline)) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE,
                "CODEX_CAPACITY_EXHAUSTED", "Selected model remained at capacity for one hour; resume explicitly to start a new retry budget", null);
    }
}
