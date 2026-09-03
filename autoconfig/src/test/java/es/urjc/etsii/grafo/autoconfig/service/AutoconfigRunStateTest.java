package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutoconfigRunStateTest {

    @Test
    void correlatesBudgetEvaluationsCandidatesAndElites() {
        var state = newState(10);
        String runId = state.prepareCoordinator(7, true);
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
        assertEquals(2, candidate.evaluations().total());
        assertEquals(1, candidate.evaluations().slow());

        state.publishElites(
                runId,
                3,
                List.of(new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm"))),
                false
        );
        assertEquals(3, state.eliteSnapshot().iteration());
        assertEquals(1, state.candidate("12").elitePosition());

        state.publishElites(
                runId,
                null,
                List.of(new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm"))),
                true
        );
        state.markCompleted();

        assertTrue(state.eliteSnapshot().finalSnapshot());
        assertEquals(AutoconfigRunState.RunStatus.COMPLETED, state.status().state());
    }

    @Test
    void retainsBoundedCompletedHistoryWithoutLosingAggregateCounts() {
        var state = newState(2);
        state.prepareCoordinator(1, true);
        state.markRunning(10);

        for (int i = 1; i <= 3; i++) {
            long evaluation = state.evaluationStarted(configuration(String.valueOf(i), i));
            state.evaluationSucceeded(evaluation, i, 0.1, 0);
        }

        var page = state.evaluations(null, null, null, null, null);
        assertTrue(page.historyTruncated());
        assertEquals(2, page.oldestRetainedId());
        assertEquals(3, page.latestId());
        assertEquals(2, page.evaluations().size());
        assertEquals(3, state.status().evaluations().succeeded());
        assertNotNull(state.candidate("1"));
    }

    @Test
    void rejectsStaleEliteSnapshotsAndInvalidPagination() {
        var state = newState(10);
        String runId = state.prepareCoordinator(1, true);
        state.markRunning(10);
        state.publishElites(runId, 4, List.of(), false);

        assertThrows(
                IllegalStateException.class,
                () -> state.publishElites(runId, 3, List.of(), false)
        );
        assertThrows(
                IllegalStateException.class,
                () -> state.publishElites("other-run", 5, List.of(), false)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> state.evaluations(-1L, 10, null, null, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> state.evaluations(0L, 501, null, null, null)
        );
    }

    @Test
    void preservesCurrentEliteSnapshotWhenAReplacementIsInvalid() {
        var state = newState(10);
        String runId = state.prepareCoordinator(1, true);
        state.markRunning(10);

        for (String configurationId : List.of("12", "13")) {
            long evaluation = state.evaluationStarted(configuration(configurationId, 1));
            state.evaluationSucceeded(evaluation, 1, 0.1, 0);
        }
        state.publishElites(
                runId,
                1,
                List.of(
                        new EliteConfiguration("12", Map.of("ROOT", "TestAlgorithm")),
                        new EliteConfiguration("13", Map.of("ROOT", "TestAlgorithm"))
                ),
                false
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> state.publishElites(
                        runId,
                        2,
                        List.of(
                                new EliteConfiguration("13", Map.of("ROOT", "TestAlgorithm")),
                                new EliteConfiguration("12", Map.of("ROOT", "DifferentAlgorithm"))
                        ),
                        false
                )
        );

        assertEquals(1, state.eliteSnapshot().iteration());
        assertEquals("12", state.eliteSnapshot().elites().getFirst().configurationId());
        assertEquals(1, state.candidate("12").elitePosition());
        assertEquals(2, state.candidate("13").elitePosition());
    }

    @Test
    void acceptsParameterOnlyCandidatesForCustomIraceBuilders() {
        var state = newState(10);
        String runId = state.prepareCoordinator(0, false);
        state.markRunning(10);
        var parameters = Map.of("alpha", "0.1", "strategy", "custom");

        long evaluation = state.evaluationStarted(configuration("7", 1, parameters));
        state.evaluationSucceeded(evaluation, 2.5, 0.1, 0);
        state.publishElites(
                runId,
                null,
                List.of(new EliteConfiguration("7", parameters)),
                true
        );
        state.markCompleted();

        assertNull(state.candidate("7").algorithm());
        assertNull(state.candidate("7").decodeError());
        assertTrue(state.eliteSnapshot().finalSnapshot());
        assertEquals(AutoconfigRunState.RunStatus.COMPLETED, state.status().state());
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
