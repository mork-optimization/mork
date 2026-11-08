package es.urjc.etsii.grafo.autoconfig.autollm;

import java.util.*;
import java.nio.file.*;

/** Common evidence and usage accounting for generation backends. */
public final class GenerationEvidence {
    private GenerationEvidence() {}
    public record Tokens(Long inputTokens, Long cachedInputTokens, Long cacheWriteInputTokens,
                         Long outputTokens, Long reasoningOutputTokens, Long totalTokens) {
        public boolean complete() { return inputTokens != null && cachedInputTokens != null && cacheWriteInputTokens != null && outputTokens != null && reasoningOutputTokens != null && totalTokens != null; }
    }
    public record Summary(String status, Tokens tokens, int attempts, int completeAttempts, int partialAttempts, int unavailableAttempts) {}
    public record Conversation(String sessionId, String parentSessionId, String path, String sha256, String cliVersion, List<String> models) {}
    public record Evidence(String status, String sessionId, List<Conversation> conversations, Summary usage, String failure) {}
    public record Entry(String key, String sessionId, String source, Tokens tokens) {}
    public record Usage(Summary summary, List<Entry> entries, List<Conversation> conversations, List<Map<String,Object>> cliReportedUsage, List<String> notes) {}
    public static class ArchiveException extends IllegalStateException {
        public ArchiveException(String message, Throwable cause) { super(message, cause); }
    }

    private static List<Long> fields(Tokens counts) { return Arrays.asList(counts.inputTokens(), counts.cachedInputTokens(), counts.cacheWriteInputTokens(), counts.outputTokens(), counts.reasoningOutputTokens(), counts.totalTokens()); }
    private static Tokens from(List<Long> values) { return new Tokens(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5)); }
    private static Tokens total(Collection<Entry> entries) {
        var sums = new ArrayList<Long>(Arrays.asList(null,null,null,null,null,null));
        for (var entry : entries) {
            var values = fields(entry.tokens());
            for (int i = 0; i < values.size(); i++) if (values.get(i) != null) sums.set(i, Math.addExact(sums.get(i) == null ? 0L : sums.get(i), values.get(i)));
        }
        return from(sums);
    }
    static Summary summary(Collection<Entry> entries, boolean complete) {
        for (var entry : entries) complete &= entry.tokens().complete();
        String status = entries.isEmpty() ? "UNAVAILABLE" : complete ? "COMPLETE" : "PARTIAL";
        return new Summary(status, total(entries), 1, status.equals("COMPLETE") ? 1 : 0, status.equals("PARTIAL") ? 1 : 0, status.equals("UNAVAILABLE") ? 1 : 0);
    }

    /** Rebuilds summaries from immutable attempt artifacts, never from the live Codex cache. */
    public static void report(AutoLlmCampaign campaign, Path root) {
        var pooled = new LinkedHashMap<String,Entry>();
        var trials = new ArrayList<Map<String,Object>>();
        int complete = 0, partial = 0, unavailable = 0;
        for (var trial : campaign.trials) {
            var entries = new LinkedHashMap<String,Entry>();
            int tc = 0, tp = 0, tu = 0;
            var attempts = new ArrayList<Map<String,Object>>();
            for (var attempt : trial.attempts) {
                if (!attempt.stage.generation()) continue;
                Path directory = root.resolve(attempt.directory);
                if (Files.isRegularFile(directory.resolve("evidence.json"))) attempt.generationEvidence = AutoLlmFileUtil.read(directory.resolve("evidence.json"), Evidence.class);
                Usage usage = Files.isRegularFile(directory.resolve("usage.json")) ? AutoLlmFileUtil.read(directory.resolve("usage.json"), Usage.class) : new Usage(summary(List.of(), false), List.of(), List.of(), List.of(), List.of("Legacy or unfinished attempt has no usage artifact"));
                switch (usage.summary().status()) { case "COMPLETE" -> tc++; case "PARTIAL" -> tp++; default -> tu++; }
                for (var entry : usage.entries()) { entries.putIfAbsent(entry.key(), entry); pooled.putIfAbsent(entry.key(), entry); }
                attempts.add(Map.of("attempt", attempt.number, "stage", attempt.stage, "status", attempt.status, "directory", attempt.directory, "usage", usage.summary()));
            }
            trial.generationUsage = aggregate(entries.values(), tc, tp, tu);
            AutoLlmFileUtil.write(root.resolve(trial.directory == null ? "trials/%03d".formatted(trial.number) : trial.directory).resolve("llm-usage.json"), Map.of("trial", trial.number, "summary", trial.generationUsage, "attempts", attempts));
            trials.add(Map.of("trial", trial.number, "status", trial.status, "summary", trial.generationUsage, "attempts", attempts));
            complete += tc; partial += tp; unavailable += tu;
        }
        campaign.generationUsage = aggregate(pooled.values(), complete, partial, unavailable);
        AutoLlmFileUtil.write(root.resolve("reports/llm-usage.json"), Map.of("campaignId", campaign.id, "summary", campaign.generationUsage, "trials", trials));
    }
    static Summary aggregate(Collection<Entry> entries, int complete, int partial, int unavailable) {
        int attempts = complete + partial + unavailable;
        return new Summary(attempts == 0 ? "NOT_STARTED" : entries.isEmpty() ? "UNAVAILABLE" : partial + unavailable == 0 ? "COMPLETE" : "PARTIAL", total(entries), attempts, complete, partial, unavailable);
    }
}
