package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.StringReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmReportChartsTest {
    @TempDir Path root;
    private AutoLlmPortableReport.Snapshot snapshot(AutoLlmCampaign campaign) {
        return AutoLlmPortableReport.build(campaign, root, new AutoLlmPortableReport.Selection("cost", FMode.MINIMIZE), Instant.parse("2026-11-01T01:00:00Z"));
    }
    private static AutoLlmPortableReport.Attempt attempt(int number, AutoLlmCampaign.AttemptStage stage, long start, Long duration) {
        var saved = new AutoLlmCampaign.Attempt(); saved.number = number; saved.stage = stage;
        saved.startedAt = Instant.ofEpochMilli(start).toString(); saved.status = AutoLlmCampaign.AttemptStatus.SUCCEEDED;
        if (duration != null) saved.finishedAt = Instant.ofEpochMilli(start + duration).toString();
        return new AutoLlmPortableReport.Attempt(saved, duration, List.of(), null, null, null);
    }
    private static AutoLlmPortableReport.Trial trial(int number, Long elapsed, AutoLlmPortableReport.Attempt... attempts) {
        var saved = new AutoLlmCampaign.Trial(); saved.number = number; saved.targetId = "worker";
        for (var attempt : attempts) saved.attempts.add(attempt.saved());
        return new AutoLlmPortableReport.Trial(saved, elapsed, 0, 0, List.of(), List.of(attempts), List.of(), null, null, List.of(), "Not compared", List.of(), List.of());
    }
    private static AutoLlmPortableReport.Attempt usage(AutoLlmPortableReport.Attempt attempt, String status, GenerationEvidence.Entry... entries) {
        var summary = GenerationEvidence.aggregate(List.of(entries), status.equals("COMPLETE") ? 1 : 0, status.equals("PARTIAL") ? 1 : 0, status.equals("UNAVAILABLE") ? 1 : 0);
        var usage = new GenerationEvidence.Usage(summary, List.of(entries), List.of(), List.of(), List.of());
        return new AutoLlmPortableReport.Attempt(attempt.saved(), attempt.millis(), List.of(), null, AutoLlmFileUtil.JSON.valueToTree(usage), null);
    }
    private static GenerationEvidence.Entry entry(String key, long input, long output) {
        return new GenerationEvidence.Entry(key, "thread", "fixture", new GenerationEvidence.Tokens(input, input / 2, 0L, output, output / 2, input + output));
    }

    @Test void nestedAttemptsUseSeparateLanesAndGapsUseTheUnionOfIntervals() {
        var row = AutoLlmReportMetrics.timeline(trial(1, 170L,
                attempt(1, AutoLlmCampaign.AttemptStage.GENERATION, 100, 100L),
                attempt(2, AutoLlmCampaign.AttemptStage.LOCAL_PREPARATION, 120, 30L),
                attempt(3, AutoLlmCampaign.AttemptStage.REPAIR, 220, 30L),
                attempt(4, AutoLlmCampaign.AttemptStage.TUNING, 260, null)));
        assertEquals(2, row.lanes()); assertEquals(0, row.spans().get(0).lane()); assertEquals(1, row.spans().get(1).lane()); assertEquals(0, row.spans().get(2).lane());
        assertEquals(List.of(new AutoLlmReportMetrics.Gap(200, 220), new AutoLlmReportMetrics.Gap(250, 270)), row.gaps());
        assertEquals(100, row.start()); assertEquals(270, row.end());
    }
    @Test void incompleteIntervalsDoNotMoveTheTrialOriginAndLiveAttemptsStopAtTheSnapshot() {
        var c = AutoLlmReportFixture.campaign(root); var t = c.trials.getFirst();
        t.attempts.getFirst().finishedAt = null;
        assertEquals(90000L, snapshot(c).trials().getFirst().elapsed());
        c.status = AutoLlmCampaign.CampaignStatus.RUNNING; t.status = AutoLlmCampaign.TrialStatus.COMPARISON;
        var a = t.attempts.getLast(); a.status = AutoLlmCampaign.AttemptStatus.RUNNING; a.finishedAt = null;
        var row = AutoLlmReportMetrics.timeline(snapshot(c).trials().getFirst());
        assertEquals(Instant.parse("2026-11-01T01:00:00Z").toEpochMilli(), row.end());
        assertEquals(1, row.trial().unknownDurations());
    }
    @Test void generationRepairBreakdownDeduplicatesAcrossAttemptsIncludingFailures() {
        var first = usage(attempt(1, AutoLlmCampaign.AttemptStage.GENERATION, 0, 100L), "COMPLETE", entry("a", 100, 30));
        first.saved().status = AutoLlmCampaign.AttemptStatus.FAILED;
        var repair = usage(attempt(2, AutoLlmCampaign.AttemptStage.REPAIR, 200, 200L), "COMPLETE", entry("a", 100, 30), entry("b", 200, 50));
        repair.saved().status = AutoLlmCampaign.AttemptStatus.INTERRUPTED;
        var repeated = usage(attempt(3, AutoLlmCampaign.AttemptStage.REPAIR, 500, 20L), "COMPLETE", entry("b", 200, 50));
        var groups = AutoLlmReportMetrics.usageStages(trial(1, 520L, repeated, repair, first));
        assertEquals(130, groups.get(0).usage().tokens().totalTokens());
        assertEquals(250, groups.get(1).usage().tokens().totalTokens());
        assertEquals(220, groups.get(1).knownMillis()); assertEquals(2, groups.get(1).usage().attempts());
        assertEquals(380, groups.get(0).usage().tokens().totalTokens() + groups.get(1).usage().tokens().totalTokens());
    }
    @Test void duplicateOnlyRepairsHaveKnownZeroAdditionalUsageButMissingEvidenceDoesNot() {
        var first = usage(attempt(1, AutoLlmCampaign.AttemptStage.GENERATION, 0, 100L), "COMPLETE", entry("a", 100, 30));
        var duplicate = usage(attempt(2, AutoLlmCampaign.AttemptStage.REPAIR, 200, 100L), "COMPLETE", entry("a", 100, 30));
        var repair = AutoLlmReportMetrics.usageStages(trial(1, 300L, first, duplicate)).get(1);
        assertEquals("COMPLETE", repair.usage().status()); assertEquals(0L, repair.usage().tokens().totalTokens());
        var missing = AutoLlmReportMetrics.usageStages(trial(1, 300L, first, attempt(2, AutoLlmCampaign.AttemptStage.REPAIR, 200, null))).get(1);
        assertEquals("UNAVAILABLE", missing.usage().status()); assertNull(missing.usage().tokens().totalTokens()); assertEquals(1, missing.unknownDurations());
    }
    @Test void partialCountersAndCorruptUsageRemainExplicitlyIncomplete() {
        var partial = new GenerationEvidence.Entry("partial", "thread", "fixture", new GenerationEvidence.Tokens(10L, null, null, null, null, null));
        var a = usage(attempt(1, AutoLlmCampaign.AttemptStage.GENERATION, 0, 100L), "PARTIAL", partial);
        var b = new AutoLlmPortableReport.Attempt(attempt(2, AutoLlmCampaign.AttemptStage.GENERATION, 200, 100L).saved(), 100L, List.of(), null, AutoLlmFileUtil.JSON.valueToTree("corrupt"), null);
        var group = AutoLlmReportMetrics.usageStages(trial(1, 300L, a, b)).getFirst();
        assertEquals("PARTIAL", group.usage().status()); assertEquals(10, group.usage().tokens().inputTokens()); assertNull(group.usage().tokens().totalTokens());
        assertEquals(1, group.usage().partialAttempts()); assertEquals(1, group.usage().unavailableAttempts());
        var repeated = usage(attempt(3, AutoLlmCampaign.AttemptStage.REPAIR, 400, 100L), "PARTIAL", partial);
        var repair = AutoLlmReportMetrics.usageStages(trial(1, 500L, a, repeated)).get(1);
        assertEquals(0L, repair.usage().tokens().inputTokens()); assertNull(repair.usage().tokens().totalTokens());
    }
    @Test void failuresAreCountedPerAttemptAndKeepInterruptionsAndLimitsSeparate() {
        var failed = attempt(1, AutoLlmCampaign.AttemptStage.LOCAL_PREPARATION, 0, 100L); failed.saved().status = AutoLlmCampaign.AttemptStatus.FAILED;
        failed.saved().problem = new AutoLlmFailureUtil.Failure(AutoLlmFailureUtil.Category.COMPONENT, "BUILD", "bad build", List.of(), false, "Repair");
        var interrupted = attempt(2, AutoLlmCampaign.AttemptStage.LOCAL_PREPARATION, 200, 100L); interrupted.saved().status = AutoLlmCampaign.AttemptStatus.INTERRUPTED;
        var limited = attempt(3, AutoLlmCampaign.AttemptStage.GENERATION, 400, 100L); limited.saved().status = AutoLlmCampaign.AttemptStatus.QUOTA_LIMIT;
        var t = trial(1, 500L, failed, interrupted, limited); t.saved().problem = failed.saved().problem;
        var groups = AutoLlmReportMetrics.failures(List.of(t));
        assertEquals(3, groups.size()); for (var group : groups) { assertEquals(1, group.attempts()); assertEquals(Set.of(1), group.trials()); }
        assertEquals("FAILED", groups.getFirst().key().outcome()); assertEquals("QUOTA_LIMIT", groups.getLast().key().outcome());
    }
    @Test void allTwentySevenTrialsAppearInEveryOverviewAndEachFigureHasAtMostTwelve() throws Exception {
        var rows = new ArrayList<AutoLlmPortableReport.Trial>();
        for (int n = 1; n <= 27; n++) {
            var row = trial(n, (long)n * 1000, attempt(1, AutoLlmCampaign.AttemptStage.GENERATION, n * 10000L, n * 1000L));
            row.saved().generationUsage = GenerationEvidence.aggregate(List.of(entry("trial-" + n, n * 100, n * 20)), 1, 0, 0); rows.add(row);
        }
        var out = new StringBuilder(); AutoLlmReportCharts.timelines(out, rows); AutoLlmReportCharts.tokens(out, rows); AutoLlmReportCharts.performance(out, rows);
        String html = out.toString();
        for (int n = 1; n <= 27; n++) assertEquals(5, occurrences(html, "data-trial=\"" + n + "\""));
        assertEquals(15, occurrences(html, "<svg "));
        var matcher = Pattern.compile("<svg .*?</svg>", Pattern.DOTALL).matcher(html);
        var factory = DocumentBuilderFactory.newInstance(); factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        while (matcher.find()) {
            String svg = matcher.group(); assertTrue(occurrences(svg, "data-trial=") <= 12);
            assertFalse(svg.contains("NaN")); assertFalse(svg.contains("Infinity"));
            assertDoesNotThrow(() -> factory.newDocumentBuilder().parse(new InputSource(new StringReader(svg))));
        }
        // The final page retains the scale of the full campaign, not just its final three trials.
        assertEquals(3, occurrences(html, ">0h 00m 27s</text>")); // elapsed chart ticks (3 pages)
        assertEquals(3, occurrences(html, ">3,240</text>"));
    }
    @Test void hostMatchedChartsPreserveGapDirectionAndExcludeFailedTrials() {
        var c = AutoLlmReportFixture.campaign(root); c.trials.get(1).targetId = "other <script>host</script>";
        var report = snapshot(c); var out = new StringBuilder(); AutoLlmReportCharts.performance(out, report.trials());
        assertTrue(out.toString().contains("-10.000%")); assertTrue(out.toString().contains("1 wins · 1 ties · 0 losses"));
        assertTrue(out.toString().contains("Excluded · see trial details")); assertTrue(out.toString().contains("other &lt;script&gt;host&lt;/script&gt;"));
        assertFalse(out.toString().contains("<script>"));
        var max = AutoLlmPortableReport.build(c, root, new AutoLlmPortableReport.Selection("quality", FMode.MAXIMIZE), Instant.now());
        out.setLength(0); AutoLlmReportCharts.performance(out, max.trials()); assertTrue(out.toString().contains(">10.000%</text>"));
    }
    @Test void emptyAndUntimedCampaignsRenderWithoutInventedBars() {
        var out = new StringBuilder(); AutoLlmReportCharts.timelines(out, List.of(trial(1, null)));
        AutoLlmReportCharts.tokens(out, List.of(trial(1, null))); AutoLlmReportCharts.performance(out, List.of());
        assertTrue(out.toString().contains("Timing unavailable")); assertTrue(out.toString().contains("Input/output unavailable")); assertFalse(out.toString().contains("class=\"chart-bar\""));
    }
    @Test void undefinedAndOverflowingPercentagesDoNotProduceInvalidSvg() {
        var rows = new ArrayList<AutoLlmPortableReport.Trial>();
        for (Double gap : Arrays.asList(null, Double.POSITIVE_INFINITY, Double.NaN, 1e300, 0.0)) {
            var t = trial(rows.size() + 1, null);
            var comparison = new AutoLlmComparisonReport.Run(t.saved().number, "worker", 1, "journal", List.of(), 0, 1, 0, gap == null ? 1 : 0, gap);
            rows.add(new AutoLlmPortableReport.Trial(t.saved(), null, 0, 0, List.of(), List.of(), List.of(), null, comparison, List.of(), null, List.of(), List.of()));
        }
        var out = new StringBuilder(); AutoLlmReportCharts.performance(out, rows); String html = out.toString();
        assertEquals(3, occurrences(html, "Percentage gap unavailable")); assertEquals(2, occurrences(html, "<circle"));
        assertFalse(html.contains("NaN")); assertFalse(html.contains("Infinity")); assertTrue(html.contains("-1.00e+300%")); assertTrue(html.contains("0.000%"));
    }
    private static int occurrences(String text, String needle) { return (text.length() - text.replace(needle, "").length()) / needle.length(); }
}
