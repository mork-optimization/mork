package es.urjc.etsii.grafo.monitoring;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome;
import es.urjc.etsii.grafo.executors.WorkUnit;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NormalExperimentRunStateTest {
    private final ResultStore<TestSolution, TestInstance> results = new ResultStore<>();
    @SuppressWarnings("unchecked")
    private final ObjectProvider<SolutionViewProvider<TestSolution, TestInstance>> provider = mock(ObjectProvider.class);
    private final NormalExperimentRunState<TestSolution, TestInstance> state = new NormalExperimentRunState<>(results, provider);
    private final Algorithm<TestSolution, TestInstance> algorithm = new Algorithm<>("algorithm") {
        @Override public TestSolution algorithm(TestInstance instance) { return new TestSolution(instance, 5); }
    };

    private void prepare(int instances, int repetitions) {
        state.prepare(new LinkedHashMap<>(Map.of("Cost", FMode.MINIMIZE, "Quality", FMode.MAXIMIZE)));
        var paths = new ArrayList<String>(); for (int i = 0; i < instances; i++) paths.add("instance-" + i);
        state.plan(new Experiment<>("experiment", getClass(), List.of(algorithm)), paths, repetitions, 100);
        state.phase(NormalExperimentRunState.Phase.RUNNING, "experiment");
    }
    private WorkUnit<TestSolution, TestInstance> unit(String path, int iteration) {
        return new WorkUnit<>(state.workId("experiment", path, algorithm.getName(), iteration), "experiment", path, algorithm, iteration);
    }
    private WorkUnitResult<TestSolution, TestInstance> finish(WorkUnit<TestSolution, TestInstance> unit, boolean success, double score) {
        var result = new WorkUnitResult<>(unit.resultId(), success, "experiment", unit.instancePath(), unit.instancePath(), algorithm,
                String.valueOf(unit.i()), success ? new TestSolution(new TestInstance(unit.instancePath()), score) : null,
                success ? Map.of("Cost", score, "Quality", score) : Map.<String, Double>of(), Map.<String, Object>of("property", 1), 2_000_000, 1_000_000, null);
        results.store(result); state.completed(result); return result;
    }
    private String run() { return state.status().runId(); }
    private String instanceId() { return state.instances(run(), null, null, 0, 25).items().getFirst().id(); }

    @Test void actualCompletionDoesNotWaitForAnEarlierRunningRepetition() {
        prepare(1, 2); var slow = unit("instance-0", 0); var fast = unit("instance-0", 1);
        state.started(slow); state.started(fast); finish(fast, true, 3);
        var counts = state.status().counts(); assertEquals(1, counts.running()); assertEquals(1, counts.succeeded());
        var detail = state.instance(run(), instanceId(), "Cost");
        assertEquals(2, detail.results().getFirst().ordinal()); assertEquals(101, detail.results().getFirst().seed());
    }
    @Test void failedResultsCountAsCompletedButDoNotEnterStatistics() {
        prepare(1, 2); var first = unit("instance-0", 0); var failed = unit("instance-0", 1);
        state.started(first); finish(first, true, 5); state.started(failed); finish(failed, false, 0);
        var summary = state.instance(run(), instanceId(), "Cost").algorithms().getFirst();
        assertEquals(1, summary.objective().count()); assertEquals(5.0, summary.objective().best()); assertEquals(1, summary.counts().failed());
        assertEquals(Outcome.COMPLETED_WITH_ERRORS, state.finish(Outcome.SUCCEEDED));
        assertEquals(2, state.status().counts().completed());
    }
    @Test void anAbortPreservesObservedCountsAndCancelsOutstandingWork() {
        prepare(2, 3); var first = unit("instance-0", 0); state.started(first); finish(first, true, 4);
        state.started(unit("instance-0", 1)); state.finish(Outcome.FAILED);
        assertEquals(NormalExperimentRunState.Phase.FAILED, state.status().phase());
        assertEquals(1, state.status().counts().completed()); assertEquals(5, state.status().counts().cancelled());
        assertEquals(0, state.status().completedInstances()); assertEquals("experiment", state.status().currentExperiment());
    }
    @Test void objectivesHaveIndependentBestResultsAndStatistics() {
        prepare(1, 3);
        for (int i = 0; i < 3; i++) { var unit = unit("instance-0", i); state.started(unit); finish(unit, true, 2 + 2 * i); }
        state.instanceStarted("experiment", "instance-0", "instance-0", Map.of("Cost", 0.0, "Quality", -2.0));
        var min = state.instance(run(), instanceId(), "Cost").algorithms().getFirst();
        var max = state.instance(run(), instanceId(), "Quality").algorithms().getFirst();
        assertEquals(2.0, min.objective().best()); assertEquals(6.0, max.objective().best()); assertEquals(4.0, min.objective().median());
        assertNull(min.objective().percentageGap()); assertEquals(-400.0, max.objective().percentageGap());
        assertNotEquals(min.bestResultId(), max.bestResultId());
    }
    @Test void allFailedAndNonFiniteScoresHaveNoBestValue() {
        prepare(1, 2); var first = unit("instance-0", 0); state.started(first); finish(first, false, 0);
        var second = unit("instance-0", 1); state.started(second); finish(second, true, Double.NaN);
        var summary = state.instance(run(), instanceId(), "Cost").algorithms().getFirst();
        assertEquals(0, summary.objective().count()); assertNull(summary.bestResultId());
        assertTrue(state.result(run(), second.resultId()).result().objectives().isEmpty());
    }
    @Test void browsingAndPagingAreNotLimitedToTenInstances() {
        prepare(12, 2); assertEquals(12, state.instances(run(), null, null, 0, 10).total());
        assertEquals(2, state.instances(run(), "experiment", null, 10, 10).items().size());
        assertEquals(24, state.results(run(), null, null, null, null, NormalExperimentRunState.ResultOrder.PLANNED, 20, 10).total());
        assertEquals(4, state.results(run(), null, null, null, null, NormalExperimentRunState.ResultOrder.PLANNED, 20, 10).items().size());
        assertThrows(ResponseStatusException.class, () -> state.instances("old-run", null, null, 0, 10));
        assertThrows(ResponseStatusException.class, () -> state.instances(run(), null, null, -1, 10));
        assertThrows(ResponseStatusException.class, () -> state.instance(run(), instanceId(), "missing"));
    }
    @Test void issueLedgerRetainsFullCorrelationAndSeverity() {
        prepare(1, 1); var unit = unit("instance-0", 0); state.started(unit); finish(unit, false, 0);
        state.issue("ERROR", "COMPUTATION", new IllegalArgumentException("bad input"), unit.resultId(), "experiment");
        var issue = state.issues(run(), "ERROR", 0, 25).items().getFirst();
        assertEquals(unit.resultId(), issue.resultId()); assertEquals("algorithm", issue.algorithmName()); assertEquals(100L, issue.seed());
        assertEquals("0", issue.iteration()); assertEquals(0, state.issues(run(), "WARNING", 0, 25).total());
    }
    @Test void renderingSurvivesReleaseAndRemovesSupersededBestViews() {
        prepare(1, 2);
        when(provider.getIfAvailable()).thenReturn(new SolutionViewProvider<>() {
            public String rendererId() { return "test"; }
            public Map<String, Object> describe(TestSolution solution) { return Map.of("score", solution.getScore()); }
        });
        var first = unit("instance-0", 0); state.started(first); finish(first, true, 5);
        var second = unit("instance-0", 1); state.started(second); finish(second, true, 5);
        results.releaseSolutionsForExperiment("experiment");
        assertEquals("AVAILABLE", state.result(run(), first.resultId()).rendering().state());
        assertEquals("UNAVAILABLE", state.result(run(), second.resultId()).rendering().state());
        assertEquals(5.0, state.result(run(), first.resultId()).rendering().data().get("score"));
    }
    @Test void largeChartsAreSampledButStatisticsAndResultQueriesAreExact() {
        prepare(1, 5001);
        for (int i = 0; i < 5001; i++) { var unit = unit("instance-0", i); state.started(unit); finish(unit, true, i); }
        var detail = state.instance(run(), instanceId(), "Cost");
        assertTrue(detail.sampled()); assertEquals(5001, detail.resultCount()); assertTrue(detail.results().size() <= 4000);
        assertEquals(5001, detail.algorithms().getFirst().objective().count());
        assertEquals(5001, state.results(run(), instanceId(), null, null, null, NormalExperimentRunState.ResultOrder.PLANNED, 0, 25).total());
        assertEquals(5001, detail.results().getLast().ordinal());
    }
    @Test void exportsAreOrderedByPlanRatherThanCompletionOrder() {
        prepare(1, 2); var first = unit("instance-0", 0); var second = unit("instance-0", 1);
        state.started(first); state.started(second); finish(second, true, 5); finish(first, true, 5);
        var ids = new ArrayList<UUID>(); for (var result : state.orderedResults("experiment")) ids.add(result.resultId());
        assertEquals(List.of(first.resultId(), second.resultId()), ids);
        assertEquals(first.resultId(), state.instance(run(), instanceId(), "Cost").algorithms().getFirst().bestResultId());
    }
    @Test void sortedResultsAreStableAcrossPagesAndMatchExports() {
        prepare(12, 1);
        var first = state.results(run(), null, null, null, null, NormalExperimentRunState.ResultOrder.INSTANCE, 0, 2);
        var next = state.results(run(), null, null, null, null, NormalExperimentRunState.ResultOrder.INSTANCE, 2, 2);
        assertEquals("instance-0", first.items().getFirst().instanceName());
        assertEquals("instance-10", next.items().getFirst().instanceName());
        var exported = state.exportResults(run(), null, null, null, null, NormalExperimentRunState.ResultOrder.INSTANCE);
        assertEquals(exported.subList(0, 2), first.items()); assertEquals(exported.subList(2, 4), next.items());
    }
}
