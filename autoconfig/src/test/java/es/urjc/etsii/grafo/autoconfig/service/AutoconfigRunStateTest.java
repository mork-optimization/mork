package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceConfig;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutoconfigRunStateTest {

    @Test
    void publishesSearchSpaceOnlyForAutomaticCoordinatorRuns() {
        var state = newState(10);
        state.prepareCoordinator(true);
        assertFalse(state.hasGeneratedSearchSpace());

        state.publishGeneratedSearchSpace(3);
        assertTrue(state.hasGeneratedSearchSpace());
        assertEquals(3, state.status().generatedParameterCount());

        state.prepareCoordinator(false);
        assertFalse(state.hasGeneratedSearchSpace());
        assertThrows(IllegalStateException.class, () -> state.publishGeneratedSearchSpace(3));
        assertThrows(IllegalArgumentException.class, () -> state.publishGeneratedSearchSpace(0));

        state.prepareWorker(true);
        assertFalse(state.hasGeneratedSearchSpace());
        assertThrows(IllegalStateException.class, () -> state.publishGeneratedSearchSpace(3));
    }

    @Test
    void correlatesMorkStateWithLatestIraceSnapshot() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(7);
        state.markRunning(20);

        long succeeded = state.evaluationStarted(configuration("12", 1));
        long rejected = state.evaluationStarted(configuration("12", 2));
        state.evaluationSucceeded(succeeded, 10.5, 0.25, 50);
        state.evaluationRejected(rejected, "INVALID_AUC", "No metric samples", 0);

        var status = state.status();
        assertEquals(2, status.budget().used());
        assertEquals(18, status.budget().remaining());
        assertEquals(1, status.evaluations().succeeded());
        assertEquals(1, status.evaluations().rejected());
        assertEquals(1, status.evaluations().slow());
        assertEquals(7, status.generatedParameterCount());

        var candidate = state.candidate("12");
        assertNotNull(candidate.algorithm());
        assertEquals(1, candidate.evaluations().succeeded());
        assertEquals(1, candidate.evaluations().rejected());
        assertEquals(1, candidate.evaluations().slow());

        var progress = experimentProgress(6, 20, 2, 18);
        state.publishProgress(
                runId,
                3,
                List.of(new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm"))),
                progress
        );
        assertEquals(3, state.eliteSnapshot().iteration());
        assertEquals(1, state.eliteSnapshot().elites().getFirst().position());
        assertSame(progress, state.status().irace().progress());

        state.publishFinalElites(
                runId,
                List.of(new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm")))
        );
        state.markCompleted();

        assertTrue(state.eliteSnapshot().finalSnapshot());
        assertSame(progress, state.status().irace().progress());
        assertEquals(AutoconfigRunState.RunStatus.COMPLETED, state.status().state());
    }

    @Test
    void retainsBoundedCompletedHistoryWithoutLosingAggregateCounts() {
        var state = newState(2);
        state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);

        for (int i = 1; i <= 3; i++) {
            long evaluation = state.evaluationStarted(configuration(String.valueOf(i), i));
            state.evaluationSucceeded(evaluation, i, 0.1, 0);
        }

        var page = state.evaluations(null, null);
        assertTrue(page.historyTruncated());
        assertEquals(2, page.oldestRetainedId());
        assertEquals(3, page.latestId());
        assertEquals(2, page.evaluations().size());
        assertEquals(3, state.status().evaluations().succeeded());
        assertNotNull(state.candidate("1"));
    }

    @Test
    void evaluationPagesAreRefetchableSnapshots() {
        var state = newState(10);
        state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);

        long evaluationId = state.evaluationStarted(configuration("12", 123));
        var running = state.evaluations(null, null).evaluations().getFirst();
        assertEquals(AutoconfigRunState.EvaluationState.RUNNING, running.state());
        assertEquals(123, running.seed());

        state.evaluationSucceeded(evaluationId, 4.5, 0.2, 0);
        var completed = state.evaluations(null, null).evaluations().getFirst();
        assertEquals(AutoconfigRunState.EvaluationState.SUCCEEDED, completed.state());
        assertEquals(4.5, completed.cost());
        assertNotNull(completed.finishedAt());
    }

    @Test
    void rejectsStaleSnapshotsAndInvalidPagination() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);
        state.publishProgress(runId, 4, List.of(), experimentProgress(4, 10, 0, 10));

        assertThrows(
                IllegalStateException.class,
                () -> state.publishProgress(runId, 3, List.of(), experimentProgress(4, 10, 0, 10))
        );
        assertThrows(
                IllegalStateException.class,
                () -> state.publishProgress("other-run", 5, List.of(), experimentProgress(5, 10, 0, 10))
        );
        assertThrows(IllegalArgumentException.class, () -> state.evaluations(-1L, 10));
        assertThrows(IllegalArgumentException.class, () -> state.evaluations(0L, 501));
    }

    @Test
    void acceptsRepeatedAndSkippedIterationSnapshots() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);

        state.publishProgress(runId, 1, List.of(), experimentProgress(3, 10, 0, 10));
        state.publishProgress(runId, 1, List.of(), experimentProgress(3, 10, 0, 10));
        state.publishProgress(runId, 3, List.of(), experimentProgress(3, 10, 0, 10));

        assertEquals(3, state.status().irace().iteration());
    }

    @Test
    void preservesCurrentSnapshotWhenAReplacementIsInvalid() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);

        for (String configurationId : List.of("12", "13")) {
            long evaluation = state.evaluationStarted(configuration(configurationId, 1));
            state.evaluationSucceeded(evaluation, 1, 0.1, 0);
        }
        state.publishProgress(
                runId,
                1,
                List.of(
                        new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm")),
                        new EliteConfiguration("13", Map.of("ROOT", "TestAlgorithm"))
                ),
                experimentProgress(2, 10, 2, 8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> state.publishProgress(
                        runId,
                        2,
                        List.of(
                                new EliteConfiguration("13", Map.of("ROOT", "TestAlgorithm")),
                                new EliteConfiguration("12", Map.of("ROOT", "DifferentAlgorithm"))
                        ),
                        experimentProgress(2, 10, 2, 8)
                )
        );

        assertEquals(1, state.eliteSnapshot().iteration());
        assertEquals("12", state.eliteSnapshot().elites().getFirst().configurationId());
        assertEquals(1, state.eliteSnapshot().elites().getFirst().position());
    }

    @Test
    void rejectedSnapshotDoesNotRegisterCandidates() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> state.publishProgress(
                        runId,
                        1,
                        List.of(
                                new EliteConfiguration("new-valid", Map.of("ROOT", "TestAlgorithm")),
                                new EliteConfiguration("duplicate", Map.of("ROOT", "TestAlgorithm")),
                                new EliteConfiguration("duplicate", Map.of("ROOT", "TestAlgorithm"))
                        ),
                        experimentProgress(2, 10, 0, 10)
                )
        );

        assertNull(state.candidate("new-valid"));
        assertNull(state.candidate("duplicate"));
    }

    @Test
    void acceptsMismatchedAndTimeBudgetProgressWithoutChangingMorkBudget() {
        var state = newState(10);
        String runId = state.prepareCoordinator(true);
        state.publishGeneratedSearchSpace(1);
        state.markRunning(10);
        long evaluation = state.evaluationStarted(configuration("12", 1));
        state.evaluationSucceeded(evaluation, 1, 0.1, 0);

        var mismatched = experimentProgress(3, 20, 2, 18);
        state.publishProgress(runId, 1, List.of(), mismatched);
        assertEquals(10, state.status().budget().maximum());
        assertEquals(1, state.status().budget().used());
        assertSame(mismatched, state.status().irace().progress());

        var timeBudget = new IraceProgressDetails(
                3, 0, 1, 99, true, 10, 1,
                100, 1, 99.0, 1.0
        );
        state.publishProgress(runId, 2, List.of(), timeBudget);
        assertSame(timeBudget, state.status().irace().progress());
        assertEquals(9, state.status().budget().remaining());
    }

    @Test
    void acceptsParameterOnlyCandidatesForCustomIraceBuilders() {
        var state = newState(10);
        String runId = state.prepareCoordinator(false);
        state.markRunning(10);
        var parameters = Map.of("alpha", "0.1", "strategy", "custom");

        long evaluation = state.evaluationStarted(configuration("7", 1, parameters));
        state.evaluationSucceeded(evaluation, 2.5, 0.1, 0);
        state.publishFinalElites(runId, List.of(new EliteConfiguration("7", parameters)));
        state.markCompleted();

        assertNull(state.candidate("7").algorithm());
        assertNull(state.candidate("7").decodeError());
        assertTrue(state.eliteSnapshot().finalSnapshot());
        assertNull(state.status().irace().progress());
    }

    @Test
    void workerHasNoCoordinatorLifecycleOrBudget() {
        var state = newState(10);
        state.prepareWorker(false);

        var status = state.status();
        assertEquals(AutoconfigRunState.Role.WORKER, status.role());
        assertEquals(AutoconfigRunState.RunStatus.NOT_STARTED, status.state());
        assertEquals(0, status.budget().maximum());
        assertFalse(status.irace().finalSnapshot());
        assertNull(status.irace().progress());
    }

    private static IraceProgressDetails experimentProgress(
            int iterations,
            int maximum,
            int used,
            int remaining
    ) {
        return new IraceProgressDetails(
                iterations,
                maximum,
                used,
                remaining,
                false,
                Math.max(used, 1),
                used,
                0,
                0,
                null,
                null
        );
    }

    private static AutoconfigRunState newState(int historyLimit) {
        var builder = mock(AutomaticAlgorithmBuilder.class);
        var objectMapper = new ObjectMapper();
        when(builder.asJsonTree(any())).thenAnswer(invocation -> {
            var config = (AlgorithmConfiguration) invocation.getArgument(0);
            var node = objectMapper.createObjectNode();
            node.put("$component", config.getValue("ROOT", "Unknown"));
            return node;
        });
        var iraceConfig = new IraceConfig();
        iraceConfig.setApiEvaluationHistoryLimit(historyLimit);
        return new AutoconfigRunState(builder, iraceConfig);
    }

    private static IraceRuntimeConfiguration configuration(String configurationId, long seed) {
        return configuration(configurationId, seed, Map.of("ROOT", "TestAlgorithm"));
    }

    private static IraceRuntimeConfiguration configuration(
            String configurationId,
            long seed,
            Map<String, String> parameters
    ) {
        return new IraceRuntimeConfiguration(
                configurationId,
                "instance-1",
                seed,
                "instance.dat",
                new AlgorithmConfiguration(parameters)
        );
    }
}
