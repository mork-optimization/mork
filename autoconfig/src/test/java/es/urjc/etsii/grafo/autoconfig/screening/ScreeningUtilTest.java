package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.TimeValue;
import org.junit.jupiter.api.Test;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;
import static org.junit.jupiter.api.Assertions.*;

class ScreeningUtilTest {
    private final InstanceScreeningConfig config = new InstanceScreeningConfig(null, null, null, null, null, null, null, null, null, null, null);
    private Run run(long seed, double value, long nanos) {
        return new Run("frozen", "instance", seed, true, value, config.horizonNanos(), List.of(new TimeValue(nanos, value)), null);
    }
    private List<Run> confirmations(int fast, long early, long late) {
        var runs = new ArrayList<Run>();
        for (int i = 1; i <= 30; i++) runs.add(run(i, 10, i <= fast ? early : late));
        return runs;
    }
    @Test void firstPassBoundaryIsInclusiveAndSlowerInputsAreRetainedWithoutConfirmations() {
        assertTrue(ScreeningUtil.candidate(run(0, 10, 30_000_000), config, FMode.MINIMIZE));
        assertFalse(ScreeningUtil.candidate(run(0, 10, 30_000_001), config, FMode.MINIMIZE));
        assertEquals("RETAINED", ScreeningUtil.classify(run(0, 10, 30_000_001), List.of(), config, FMode.MINIMIZE).classification());
    }
    @Test void excludes29Of30ButNot28AndDoesNotCountTheInitialRun() {
        var initial = run(0, 10, 1);
        assertEquals("TRIVIAL", ScreeningUtil.classify(initial, confirmations(29, 9_999_999, 20_000_000), config, FMode.MINIMIZE).classification());
        assertEquals("RETAINED", ScreeningUtil.classify(initial, confirmations(28, 9_999_999, 20_000_000), config, FMode.MINIMIZE).classification());
        assertEquals("RETAINED", ScreeningUtil.classify(initial, confirmations(30, 10_000_000, 20_000_000), config, FMode.MINIMIZE).classification());
    }
    @Test void twoCompletedMissesRetainEarlyButOneMissCanStillAllowExclusion() {
        var initial = run(0, 10, 1_000);
        var runs = new ArrayList<Run>();
        runs.add(run(1, 10, 10_000_000));
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs.add(run(2, 10, 20_000_000));
        var decision = ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE);
        assertEquals("RETAINED", decision.classification());
        assertEquals(2, decision.confirmations());
        assertEquals(0, decision.fastHits());
        assertEquals(0, decision.failures());
        assertTrue(decision.reason().contains("stopped early"));
    }
    @Test void sequence30Then30Then29TurnsTheFirstTwoFastHitsIntoMisses() {
        var initial = run(0, 30, 1_000);
        var firstTwo = List.of(run(1, 30, 1_000), run(2, 30, 1_000));
        var before = ScreeningUtil.classify(initial, firstTwo, config, FMode.MINIMIZE);
        assertEquals(2, before.fastHits());
        assertEquals("INCONCLUSIVE", before.classification());
        for (long thirdTtb : List.of(1_000L, 20_000_000L)) {
            var runs = new ArrayList<>(firstTwo);
            runs.add(run(3, 29, thirdTtb));
            var after = ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE);
            assertEquals(29, after.bestObserved());
            assertEquals(thirdTtb < config.thresholdNanos() ? 1 : 0, after.fastHits());
            assertEquals(3, after.confirmations());
            assertEquals("RETAINED", after.classification());
        }
    }
    @Test void aNewCommonTargetReevaluatesPreviouslyFastConfirmations() {
        var initial = run(0, 10, 1_000);
        var first = run(1, 10, 1_000);
        assertEquals(1, ScreeningUtil.classify(initial, List.of(first), config, FMode.MINIMIZE).fastHits());
        var decision = ScreeningUtil.classify(initial, List.of(first, run(2, 9, 20_000_000)), config, FMode.MINIMIZE);
        assertEquals("RETAINED", decision.classification());
        assertEquals(9, decision.bestObserved());
        assertEquals(0, decision.fastHits());
        decision = ScreeningUtil.classify(initial, List.of(first, run(2, 11, 20_000_000)), config, FMode.MAXIMIZE);
        assertEquals("RETAINED", decision.classification());
        assertEquals(11, decision.bestObserved());
        assertEquals(0, decision.fastHits());
    }
    @Test void earlyRetentionUsesTheConfiguredStrictFractionWithoutRoundingItsProduct() {
        var settings = new InstanceScreeningConfig(null, null, null, null, null, 50, .58, null, null, null, null);
        var runs = new ArrayList<Run>();
        for (int i = 1; i <= 20; i++) runs.add(run(i, 10, 20_000_000));
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(run(0, 10, 1_000), runs, settings, FMode.MINIMIZE).classification());
        runs.add(run(21, 10, 20_000_000));
        assertEquals("RETAINED", ScreeningUtil.classify(run(0, 10, 1_000), runs, settings, FMode.MINIMIZE).classification());
    }
    @Test void partialFastConfirmationsCanNeverExcludeAnInstance() {
        var decision = ScreeningUtil.classify(run(0, 10, 1_000), confirmations(30, 1_000, 1_000).subList(0, 29), config, FMode.MINIMIZE);
        assertEquals("INCONCLUSIVE", decision.classification());
    }
    @Test void invalidPartialConfirmationsStayInconclusiveEvenWithTwoMisses() {
        var initial = run(0, 10, 1_000);
        var runs = new ArrayList<>(List.of(run(1, 10, 20_000_000), run(2, 10, 20_000_000)));
        runs.add(new Run("frozen", "instance", 3, false, null, 0, List.of(), "failed"));
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs.set(2, null);
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs.set(2, runs.getFirst());
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
    }
    @Test void fastPoorLocalOptimaDoNotMeetTheCommonTarget() {
        var runs = confirmations(30, 1_000, 1_000);
        runs.set(0, run(1, 9, 100_000_000));
        var result = ScreeningUtil.classify(run(0, 10, 1_000), runs, config, FMode.MINIMIZE);
        assertEquals(9, result.bestObserved()); assertEquals(0, result.fastHits()); assertEquals("RETAINED", result.classification());
    }
    @Test void initialBestIsPartOfTheTargetEvenIfNoConfirmationReachesIt() {
        var result = ScreeningUtil.classify(run(0, 9, 1_000), confirmations(30, 1_000, 1_000), config, FMode.MINIMIZE);
        assertEquals(9, result.bestObserved()); assertEquals(0, result.fastHits()); assertEquals("RETAINED", result.classification());
    }
    @Test void maximizationUsesTheOppositeObjectiveDirection() {
        var runs = confirmations(30, 1_000, 1_000);
        assertEquals("TRIVIAL", ScreeningUtil.classify(run(0, 9, 1_000), runs, config, FMode.MAXIMIZE).classification());
        runs.set(29, run(30, 11, 50_000_000));
        assertEquals("RETAINED", ScreeningUtil.classify(run(0, 9, 1_000), runs, config, FMode.MAXIMIZE).classification());
    }
    @Test void failedMissingDuplicateAndForeignRunsAreInconclusive() {
        var initial = run(0, 10, 1_000);
        var runs = confirmations(30, 1_000, 1_000);
        runs.set(0, new Run("frozen", "instance", 1, false, null, 0, List.of(), "failed"));
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs = confirmations(30, 1_000, 1_000); runs.removeLast();
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs = confirmations(30, 1_000, 1_000); runs.set(29, runs.getFirst());
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
        runs.set(29, new Run("frozen", "different", 30, true, 10.0, config.horizonNanos(), List.of(new TimeValue(1, 10)), null));
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, runs, config, FMode.MINIMIZE).classification());
    }
    @Test void missingNegativeUnsortedAndInconsistentCurvesNeverBecomeZeroTtb() {
        for (List<TimeValue> curve : List.of(List.<TimeValue>of(), List.of(new TimeValue(-1, 10)),
                List.of(new TimeValue(20, 20), new TimeValue(10, 10)), List.of(new TimeValue(1, 20)), List.of(new TimeValue(1, Double.NaN)))) {
            var initial = new Run("frozen", "instance", 0, true, 10.0, config.horizonNanos(), curve, null);
            assertEquals("INCONCLUSIVE", ScreeningUtil.classify(initial, List.of(), config, FMode.MINIMIZE).classification());
        }
        var tooShort = new Run("frozen", "instance", 0, true, 10.0, 1_000, List.of(new TimeValue(1, 10)), null);
        assertEquals("INCONCLUSIVE", ScreeningUtil.classify(tooShort, List.of(), config, FMode.MINIMIZE).classification());
    }
    @Test void targetTimeIsFirstAttainmentAndPostHorizonObservationsDoNotCount() {
        var r = new Run("frozen", "instance", 0, true, 10.0, config.horizonNanos() + 5,
                List.of(new TimeValue(1, 20), new TimeValue(2, 10), new TimeValue(3, 10)), null);
        assertEquals(2, ScreeningUtil.firstAttainment(r, 10, FMode.MINIMIZE, config.horizonNanos()));
        assertNull(ScreeningUtil.firstAttainment(run(0, 10, config.horizonNanos() + 1), 10, FMode.MINIMIZE, config.horizonNanos()));
    }
    @Test void ttbSummariesExposeUnreachedTargetsSeparately() {
        var runs = List.of(run(1, 10, 2), run(2, 10, 4), run(3, 11, 1));
        var result = ScreeningUtil.summarize(runs, 10.0, FMode.MINIMIZE, config.horizonNanos());
        assertEquals(2, result.attained()); assertEquals(2, result.medianNanos());
        assertEquals(4, result.p95Nanos()); assertEquals(4, result.maxNanos());
        assertNull(ScreeningUtil.summarize(runs, 9.0, FMode.MINIMIZE, config.horizonNanos()).medianNanos());
    }
}
