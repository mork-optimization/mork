package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.exception.IllegalAlgorithmConfigException;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IraceTargetEvaluatorTest {

    private SolverConfig solverConfig;
    private AlgorithmBuilder<TestSolution, TestInstance> algorithmBuilder;
    private AutoconfigRunState runState;
    private IraceTargetEvaluator<TestSolution, TestInstance> evaluator;
    private TestInstance instance;
    private SolutionValidator<TestSolution, TestInstance> validator;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        Context.Configurator.setObjectives(Objective.of(
                "Test",
                FMode.MINIMIZE,
                TestSolution::getScore,
                TestMove::getScoreChange
        ));
        Metrics.disableMetrics();
        solverConfig = new SolverConfig();
        solverConfig.setParallelExecutor(false);
        solverConfig.setRandomType(RandomType.DEFAULT);
        var iraceConfig = new IraceConfig();
        runState = new AutoconfigRunState(mock(AutomaticAlgorithmBuilder.class));
        runState.prepareWorker(false);

        instance = new TestInstance("instance");
        var instanceManager = (InstanceManager<TestInstance>) mock(InstanceManager.class);
        when(instanceManager.requireConfiguredInstancePath("irace", "instance.dat"))
                .thenReturn("instance.dat");
        when(instanceManager.getInstance("instance.dat")).thenReturn(instance);

        algorithmBuilder = (AlgorithmBuilder<TestSolution, TestInstance>) mock(AlgorithmBuilder.class);
        var solutionBuilder = (SolutionBuilder<TestSolution, TestInstance>) mock(SolutionBuilder.class);
        validator = (SolutionValidator<TestSolution, TestInstance>) mock(SolutionValidator.class);
        when(validator.validate(any())).thenReturn(ValidationResult.ok());
        evaluator = new IraceTargetEvaluator<>(
                solverConfig,
                iraceConfig,
                instanceManager,
                List.of(solutionBuilder),
                List.of(algorithmBuilder),
                Optional.of(validator),
                Optional.empty(),
                runState
        );
    }

    @AfterEach
    void cleanUpMetrics() {
        Metrics.disableMetrics();
    }

    @Test
    @SuppressWarnings("unchecked")
    void recordsSuccessfulEvaluation() {
        var algorithm = (Algorithm<TestSolution, TestInstance>) mock(Algorithm.class);
        when(algorithmBuilder.buildFromConfig(any())).thenReturn(algorithm);
        when(algorithm.algorithm(instance)).thenReturn(new TestSolution(instance, 4.5));

        var response = evaluator.evaluateBatch(List.of(configuration("12"))).getFirst();

        assertEquals(4.5, response.getCost());
        assertEquals(1, runState.status().budget().used());
        assertEquals(1, runState.status().evaluations().succeeded());
    }

    @Test
    @SuppressWarnings("unchecked")
    void preflightDoesNotRecordEvaluation() {
        var algorithm = (Algorithm<TestSolution, TestInstance>) mock(Algorithm.class);
        when(algorithmBuilder.buildFromConfig(any())).thenReturn(algorithm);
        when(algorithm.algorithm(instance)).thenReturn(new TestSolution(instance, 4.5));

        var response = evaluator.preflightBatch(List.of(configuration("12"))).getFirst();

        assertEquals(4.5, response.getCost());
        assertEquals(0, runState.status().budget().used());
        assertNull(runState.candidate("12"));
    }

    @Test
    void recordsInvalidConfigurationAsRejected() {
        when(algorithmBuilder.buildFromConfig(any()))
                .thenThrow(new IllegalAlgorithmConfigException("invalid combination"));

        var response = evaluator.evaluateBatch(List.of(configuration("12"))).getFirst();

        assertTrue(Double.isNaN(response.getCost()));
        assertEquals(1, runState.status().evaluations().rejected());
        assertEquals(
                "INVALID_CONFIGURATION",
                runState.evaluations(null, null, null).evaluations().getFirst().reasonCode()
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsInvalidSolutionsAndContinuesTheBatch(boolean parallel) {
        solverConfig.setParallelExecutor(parallel);
        solverConfig.setnWorkers(2);
        configureMixedValidityBatch();

        var responses = evaluator.evaluateBatch(List.of(configuration("invalid", 1), configuration("valid", 2)));

        assertEquals("Inf 0", responses.getFirst().toIraceResultString());
        assertEquals(2, responses.get(1).getCost());
        assertEquals(2, runState.status().budget().used());
        assertEquals(1, runState.status().evaluations().rejected());
        assertEquals(1, runState.status().evaluations().succeeded());
        assertEquals(0, runState.status().evaluations().failed());
        var rejected = runState.evaluations(null, null, null).evaluations();
        int invalidCount = 0;
        for (var evaluation : rejected) {
            if ("INVALID_SOLUTION".equals(evaluation.reasonCode())) {
                invalidCount++;
                assertEquals("Tour violates 76 time windows", evaluation.message());
            }
        }
        assertEquals(1, invalidCount);
    }

    @Test
    void preflightRejectsInvalidSolutionsWithoutRecordingTuningWork() {
        configureMixedValidityBatch();

        var responses = evaluator.preflightBatch(List.of(configuration("invalid", 1), configuration("valid", 2)));

        assertEquals("Inf 0", responses.getFirst().toIraceResultString());
        assertEquals(2, responses.get(1).getCost());
        assertEquals(0, runState.status().budget().used());
        assertNull(runState.candidate("invalid"));
        assertNull(runState.candidate("valid"));
    }

    @Test
    void automaticModeRejectsInvalidSolutionBeforeScoringAndClearsDeadline() {
        runState.prepareWorker(true);
        solverConfig.setIgnoreInitialMillis(0);
        solverConfig.setIntervalDurationMillis(1);
        configureMixedValidityBatch();

        // There are no objective metrics: validation must reject before attempting AUC.
        var response = evaluator.evaluateBatch(List.of(configuration("invalid", 1))).getFirst();

        assertEquals("Inf 0", response.toIraceResultString());
        assertEquals("INVALID_SOLUTION", runState.evaluations(null, null, null).evaluations().getFirst().reasonCode());
        assertFalse(TimeControl.isEnabled());
    }

    private void configureMixedValidityBatch() {
        when(algorithmBuilder.buildFromConfig(any())).thenAnswer(invocation -> {
            var config = (AlgorithmConfiguration) invocation.getArgument(0);
            double score = config.getValueAsDouble("score", 0);
            return new Algorithm<TestSolution, TestInstance>("algorithm-" + score) {
                @Override public TestSolution algorithm(TestInstance instance) {
                    return new TestSolution(instance, score);
                }
            };
        });
        when(validator.validate(any())).thenAnswer(invocation -> {
            var solution = (TestSolution) invocation.getArgument(0);
            return solution.getScore() == 1 ? ValidationResult.fail("Tour violates 76 time windows") : ValidationResult.ok();
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void recordsUnexpectedExecutionFailure() {
        var algorithm = (Algorithm<TestSolution, TestInstance>) mock(Algorithm.class);
        when(algorithmBuilder.buildFromConfig(any())).thenReturn(algorithm);
        when(algorithm.algorithm(instance)).thenThrow(new IllegalStateException("execution failed"));

        assertThrows(
                IllegalStateException.class,
                () -> evaluator.evaluateBatch(List.of(configuration("12")))
        );

        assertEquals(1, runState.status().evaluations().failed());
        assertEquals(
                "EXECUTION_ERROR",
                runState.evaluations(null, null, null).evaluations().getFirst().reasonCode()
        );
    }

    @Test
    void parallelBatchPreservesRequestOrder() {
        solverConfig.setParallelExecutor(true);
        solverConfig.setnWorkers(2);
        when(algorithmBuilder.buildFromConfig(any())).thenAnswer(invocation -> {
            var configuration = (AlgorithmConfiguration) invocation.getArgument(0);
            double score = configuration.getValueAsDouble("score", 0);
            return new Algorithm<TestSolution, TestInstance>("algorithm-" + score) {
                @Override
                public TestSolution algorithm(TestInstance instance) {
                    if (score == 1) {
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
                    }
                    return new TestSolution(instance, score);
                }
            };
        });

        var responses = evaluator.preflightBatch(List.of(
                configuration("first", 1),
                configuration("second", 2)
        ));

        assertEquals(List.of(1.0, 2.0), responses.stream().map(response -> response.getCost()).toList());
    }

    @Test
    void automaticModeUsesAreaUnderTheObjectiveCurve() {
        runState.prepareWorker(true);
        solverConfig.setIgnoreInitialMillis(0);
        solverConfig.setIntervalDurationMillis(1);
        solverConfig.setLogScaleArea(false);
        Metrics.register("Test", instant -> new DeclaredObjective("Test", FMode.MINIMIZE, instant));
        @SuppressWarnings("unchecked")
        var algorithm = (Algorithm<TestSolution, TestInstance>) mock(Algorithm.class);
        when(algorithmBuilder.buildFromConfig(any())).thenReturn(algorithm);
        when(algorithm.algorithm(instance)).thenAnswer(_ -> {
            var metric = Metrics.get("Test");
            metric.add(metric.getReferenceNanoTime(), 3);
            return new TestSolution(instance, 99);
        });

        var response = evaluator.evaluateBatch(List.of(configuration("12"))).getFirst();

        assertEquals(3.0, response.getCost());
        assertEquals(1, runState.status().evaluations().succeeded());
    }

    private static IraceExecuteConfig configuration(String id) {
        return configuration(id, 0);
    }

    private static IraceExecuteConfig configuration(String id, double score) {
        return IraceExecuteConfig.of(
                id,
                1,
                "instance.dat",
                123,
                Map.of(
                        "ROOT", "TestAlgorithm",
                        "score", String.valueOf(score)
                )
        );
    }
}
