package es.urjc.etsii.grafo.autoconfig.autollm;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

/** Explicit Codex usage caps are waits, separate from ordinary process/network failures. */
public final class CodexQuotaUtil {
    private static final Pattern RESET_TIME = Pattern.compile("try again at (\\d{1,2}:\\d{2}\\s*[ap]m)", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter CLOCK_TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final Duration RETRY_INTERVAL = Duration.ofMinutes(5);
    public static final class QuotaLimitException extends IllegalStateException {
        private final Instant retryAt;
        public QuotaLimitException(String message, Instant retryAt) { super(message); this.retryAt = retryAt; }
        public Instant retryAt() { return retryAt; }
    }
    private CodexQuotaUtil() {}

    public static QuotaLimitException detect(Path output, Instant startedAt, Instant now, ZoneId zone) {
        JsonNode error = null;
        Path log = output.resolve("stdout.log");
        if (!Files.isRegularFile(log)) return null;
        try (var reader = Files.newBufferedReader(log)) {
            for (String line; (line = reader.readLine()) != null;) {
                JsonNode event = parse(line);
                if (event == null) continue;
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
        String normalized = message.replace('\u2019', '\'').toLowerCase(Locale.ROOT);
        if (!normalized.contains("you've hit your usage limit")
                && !error.path("code").asString("").equals("usage_limit_reached")
                && !error.path("codex_error_info").asString("").equals("UsageLimitExceeded")) return null;
        Instant reset = archivedReset(output, now);
        if (reset == null) {
            var match = RESET_TIME.matcher(message);
            if (match.find()) try {
                var local = startedAt.atZone(zone);
                var time = LocalTime.parse(match.group(1).toUpperCase(Locale.US).replaceAll("\\s*([AP]M)$", " $1"), CLOCK_TIME);
                var candidate = local.toLocalDate().atTime(time).atZone(zone);
                // A clock time just before launch can be an already-passing reset, not tomorrow's.
                if (candidate.toInstant().isBefore(startedAt)
                        && Duration.between(candidate.toInstant(), startedAt).compareTo(RETRY_INTERVAL) > 0) candidate = candidate.plusDays(1);
                if (candidate.toInstant().isAfter(now)) reset = candidate.toInstant();
            } catch (DateTimeException ignored) { /* An unrecognized display time uses the polling fallback. */ }
        }
        return new QuotaLimitException(message.isBlank() ? "Codex usage limit reached" : message,
                reset == null ? now.plus(RETRY_INTERVAL) : reset.plusSeconds(2));
    }

    private static Instant archivedReset(Path output, Instant now) {
        Path evidence = output.resolve("evidence.json");
        if (!Files.isRegularFile(evidence)) return null;
        var archive = AutoLlmFileUtil.JSON.readTree(read(evidence));
        var latest = new LinkedHashMap<String, JsonNode>();
        for (var conversation : archive.path("conversations")) {
            if (!conversation.path("threadId").asString("").equals(archive.path("threadId").asString(""))) continue;
            Path file = AutoLlmFileUtil.inside(output, conversation.path("path").asString());
            try (var reader = Files.newBufferedReader(file)) {
                for (String line; (line = reader.readLine()) != null;) {
                    var event = parse(line);
                    if (event == null || !event.path("type").asString("").equals("event_msg")) continue;
                    var limits = event.path("payload").path("rate_limits");
                    if (limits.isObject()) latest.put(limits.path("limit_id").asString("codex"), limits);
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        Instant reset = null;
        for (var limits : latest.values()) for (String name : new String[]{"primary", "secondary"}) {
            var window = limits.path(name);
            if (window.path("used_percent").asDouble(0) < 100 || !window.path("resets_at").isIntegralNumber()) continue;
            try {
                Instant candidate = Instant.ofEpochSecond(window.path("resets_at").longValue());
                if (candidate.isAfter(now) && (reset == null || candidate.isAfter(reset))) reset = candidate;
            } catch (DateTimeException ignored) { /* Invalid reset metadata uses the fallback. */ }
        }
        return reset;
    }
    private static String read(Path file) {
        try { return Files.readString(file); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static JsonNode parse(String line) {
        try { return AutoLlmFileUtil.JSON.readTree(line); }
        catch (tools.jackson.core.JacksonException ignored) { return null; }
    }
}
