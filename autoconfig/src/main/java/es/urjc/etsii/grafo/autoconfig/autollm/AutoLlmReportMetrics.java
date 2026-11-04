package es.urjc.etsii.grafo.autoconfig.autollm;

import java.util.*;

/** Presentation metrics derived exclusively from a portable report snapshot. */
final class AutoLlmReportMetrics {
    private AutoLlmReportMetrics() {}
    record Span(AutoLlmPortableReport.Attempt attempt, long start, long end, int lane) {}
    record Gap(long start, long end) {}
    record Timeline(AutoLlmPortableReport.Trial trial, List<Span> spans, List<Gap> gaps,
                    Long start, Long end, int lanes) {}
    record UsageStage(String name, CodexEvidenceUtil.Summary usage, long knownMillis, int unknownDurations) {}
    record FailureKey(String stage, String outcome, String category, String code) {}
    record FailureGroup(FailureKey key, int attempts, Set<Integer> trials) {}

    static Double gap(AutoLlmComparisonReport.Run run) {
        if (run == null || run.meanImprovementPercent() == null || !Double.isFinite(run.meanImprovementPercent())) return null;
        return run.meanImprovementPercent() == 0 ? 0 : -run.meanImprovementPercent();
    }

    static Timeline timeline(AutoLlmPortableReport.Trial trial) {
        var attempts = new ArrayList<>(trial.attempts());
        attempts.removeIf(a -> a.millis() == null || AutoLlmPortableReport.instant(a.saved().startedAt) == null);
        attempts.sort(Comparator.comparingLong((AutoLlmPortableReport.Attempt a) -> AutoLlmPortableReport.instant(a.saved().startedAt))
                .thenComparingInt(a -> a.saved().number));
        var spans = new ArrayList<Span>(); var gaps = new ArrayList<Gap>(); var laneEnds = new ArrayList<Long>();
        Long first = null, last = null;
        for (var attempt : attempts) {
            long start = AutoLlmPortableReport.instant(attempt.saved().startedAt), end = start + attempt.millis();
            if (first == null) first = start;
            if (last != null && start > last) gaps.add(new Gap(last, start));
            last = Math.max(last == null ? end : last, end);
            int lane = 0;
            while (lane < laneEnds.size() && laneEnds.get(lane) > start) lane++;
            if (lane == laneEnds.size()) laneEnds.add(end); else laneEnds.set(lane, end);
            spans.add(new Span(attempt, start, end, lane));
        }
        if (first != null && trial.elapsed() != null && first + trial.elapsed() > last) {
            gaps.add(new Gap(last, first + trial.elapsed())); last = first + trial.elapsed();
        }
        return new Timeline(trial, spans, gaps, first, last, Math.max(1, laneEnds.size()));
    }

    /** A response belongs to its first recorded attempt, including across generation/repair boundaries. */
    static List<UsageStage> usageStages(AutoLlmPortableReport.Trial trial) {
        var generation = new UsageGroup("Generation"); var repair = new UsageGroup("Repair");
        var seen = new HashSet<String>(); var attempts = new ArrayList<>(trial.attempts());
        attempts.sort(Comparator.comparingInt(a -> a.saved().number));
        for (var attempt : attempts) {
            if (!attempt.saved().stage.generation()) continue;
            var group = attempt.saved().stage == AutoLlmCampaign.AttemptStage.REPAIR ? repair : generation;
            if (attempt.millis() == null) group.unknown++; else group.millis += attempt.millis();
            CodexEvidenceUtil.Usage usage = null;
            try {
                if (attempt.usage() != null) usage = AutoLlmFileUtil.JSON.treeToValue(attempt.usage(), CodexEvidenceUtil.Usage.class);
                if (usage != null && (usage.summary() == null || usage.summary().status() == null || usage.entries() == null)) usage = null;
                if (usage != null) for (var entry : usage.entries()) {
                    if (entry.key() == null || entry.tokens() == null) { usage = null; break; }
                }
            } catch (RuntimeException ignored) { usage = null; /* Corrupt saved evidence cannot become a zero count. */ }
            String status = usage == null ? "UNAVAILABLE" : usage.summary().status();
            switch (status) { case "COMPLETE" -> group.complete++; case "PARTIAL" -> group.partial++; default -> group.unavailable++; }
            if (usage == null) continue;
            group.hasEntries |= !usage.entries().isEmpty();
            for (var entry : usage.entries()) {
                group.observe(entry.tokens());
                if (seen.add(entry.key())) group.entries.add(entry);
            }
        }
        return List.of(generation.result(), repair.result());
    }

    private static final class UsageGroup {
        final String name;
        final List<CodexEvidenceUtil.Entry> entries = new ArrayList<>();
        final Long[] zeroKnown = new Long[6];
        int complete, partial, unavailable, unknown;
        long millis;
        boolean hasEntries;
        UsageGroup(String name) { this.name = name; }
        void observe(CodexEvidenceUtil.Tokens tokens) {
            var values = Arrays.asList(tokens.inputTokens(), tokens.cachedInputTokens(), tokens.cacheWriteInputTokens(), tokens.outputTokens(), tokens.reasoningOutputTokens(), tokens.totalTokens());
            for (int i = 0; i < values.size(); i++) if (values.get(i) != null) zeroKnown[i] = 0L;
        }
        UsageStage result() {
            var summary = CodexEvidenceUtil.aggregate(entries, complete, partial, unavailable);
            if (entries.isEmpty() && hasEntries) {
                // Known responses were all charged to an earlier attempt; their additional usage is zero.
                summary = new CodexEvidenceUtil.Summary(partial + unavailable == 0 ? "COMPLETE" : "PARTIAL",
                        new CodexEvidenceUtil.Tokens(zeroKnown[0], zeroKnown[1], zeroKnown[2], zeroKnown[3], zeroKnown[4], zeroKnown[5]), complete + partial + unavailable,
                        complete, partial, unavailable);
            }
            return new UsageStage(name, summary, millis, unknown);
        }
    }

    static List<FailureGroup> failures(List<AutoLlmPortableReport.Trial> trials) {
        var counts = new LinkedHashMap<FailureKey, Integer>(); var affected = new LinkedHashMap<FailureKey, Set<Integer>>();
        for (var trial : trials) for (var attempt : trial.attempts()) {
            var saved = attempt.saved();
            if (saved.status == AutoLlmCampaign.AttemptStatus.RUNNING || saved.status == AutoLlmCampaign.AttemptStatus.SUCCEEDED) continue;
            var problem = saved.problem;
            var key = new FailureKey(saved.stage.name(), saved.status.name(), problem == null || problem.category() == null ? "UNKNOWN" : problem.category().name(),
                    problem == null ? "UNCLASSIFIED" : problem.code());
            counts.merge(key, 1, Integer::sum); affected.computeIfAbsent(key, ignored -> new TreeSet<>()).add(trial.saved().number);
        }
        var result = new ArrayList<FailureGroup>();
        for (var entry : counts.entrySet()) result.add(new FailureGroup(entry.getKey(), entry.getValue(), affected.get(entry.getKey())));
        return result;
    }

    static String total(CodexEvidenceUtil.Summary usage) {
        if (usage == null) return "Unavailable";
        String total = usage.tokens() == null ? "Unavailable" : Objects.toString(usage.tokens().totalTokens(), "Unavailable");
        return total + " · " + usage.status();
    }
    static String coverage(CodexEvidenceUtil.Summary usage) {
        if (usage == null) return "Unavailable";
        return usage.completeAttempts() + "/" + usage.attempts() + " complete; " + usage.partialAttempts() + " partial; "
                + usage.unavailableAttempts() + " unavailable";
    }
}
