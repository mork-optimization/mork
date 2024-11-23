package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithmBuilder;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class MultiStartAlgorithmTest {

    private final TestInstance testInstance = new TestInstance("testinstance");
    private final TestSolution testSolution = new TestSolution(testInstance);
    private static final Objective<?,TestSolution,TestInstance> maxObj = Objective.ofMaximizing("TestMax", TestSolution::getScore, TestMove::getScoreChange);
    private static final Objective<?,TestSolution,TestInstance> minObj = Objective.ofMinimizing("TestMin", TestSolution::getScore, TestMove::getScoreChange);
    private Algorithm<TestSolution, TestInstance> algorithm;

    @BeforeAll
    static void setupObjectives(){
        Context.Configurator.setObjectives(maxObj);
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUpAlgorithm() {
        algorithm = Mockito.mock(Algorithm.class);
        when(algorithm.algorithm(testInstance)).thenReturn(testSolution);
    }

    @Test
    void testIllegalParameters() {
        // Legal
        int minNumberOfIterations = 1;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;

        // Illegal
        int _minNumberOfIterations = -1;
        int _maxNumberOfIterations = 0;
        int _maxIterWithoutImprovement = 0;
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(_maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(_minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(_maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(_maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(null)
                .build(algorithm));

        Assertions.assertDoesNotThrow(() -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMinIterations(51)
                .withMaxIterations(50)
                .build(algorithm));

        Assertions.assertDoesNotThrow(() -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMinIterations(50)
                .withMaxIterations(50)
                .withObjective(maxObj)
                .build(algorithm));

        Assertions.assertDoesNotThrow(() -> new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMinIterations(50)
                .withMaxIterations(50)
                .withObjective(minObj)
                .build(algorithm));

    }

    @Test
    void checkMinimumAndMaxNumberOfIterations() {
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 80;
        int maxIterWithoutImprovement = 1000;
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, atLeast(minNumberOfIterations)).algorithm(testInstance);
        verify(algorithm, atMost(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    void checkMinimumStopNotImprovement() {
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 10_000_000;
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(1)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, times(minNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    void checkMaxNumberOfIterations() {
        int minNumberOfIterations = 1;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, times(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    void checkStopByMaxTime() {
        int maxNumberOfIterations = 10_000_000;
        int maxIterWithoutImprovement = 10_000_000;
        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        TimeControl.setMaxExecutionTime(maxTime, timeUnit);
        TimeControl.start();
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMaxIterations(maxNumberOfIterations)
                .withObjective(maxObj)
                .build(algorithm);
        long startTime = System.nanoTime();
        multistart.algorithm(testInstance);
        long endTime = System.nanoTime();
        long ellapsed = (endTime - startTime) / 1_000_000;
        TimeControl.remove();
        Assertions.assertTrue(ellapsed < 50, "Does not stop after 10 millis");
        Assertions.assertTrue(ellapsed > 5, "Stops in less than 5 millis");
    }

    @Test
    void checkStopByMaxIterWithoutImprovement() {
        int maxIterWithoutImprovement = 1;
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withObjective(maxObj)
                .build(algorithm);
        multistart.algorithm(testInstance);
        // Execute twice: First solution is null, improves, second time does not improve, end
        verify(algorithm, times(2)).algorithm(testInstance);
    }

    @Test
    void checkSetsBuilder(){
        var multistart = new MultiStartAlgorithmBuilder<TestSolution, TestInstance>()
                .withMaxIterationsWithoutImproving(100)
                .withObjective(maxObj)
                .build(algorithm);
        SolutionBuilder<TestSolution, TestInstance> builder = new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        };
        verify(algorithm, times(0)).setBuilder(any());
        multistart.setBuilder(builder);
        verify(algorithm, times(1)).setBuilder(any());
    }

}
