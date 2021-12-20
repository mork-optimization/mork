package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.solver.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.solver.create.builder.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class MultiStartAlgorithmTest {

    private final TestInstance testInstance = new TestInstance("testinstance");
    private final TestSolution testSolution = new TestSolution(testInstance);
    private Algorithm<TestSolution, TestInstance> algorithm;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUpAlgorithm() {
        algorithm = Mockito.mock(Algorithm.class);
        when(algorithm.algorithm(testInstance)).thenReturn(testSolution);
    }

    @Test
    public void testIllegalParameters() {
        // Legal
        int minNumberOfIterations = 1;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;
        int maxTime = 1;

        // Illegal
        int _minNumberOfIterations = -1;
        int _maxNumberOfIterations = 0;
        int _maxIterWithoutImprovement = 0;
        int _maxTime = 0;
        TimeUnit timeUnit = TimeUnit.DAYS;
        Assertions.assertThrows(IllegalArgumentException.class, () -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(_maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(_minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(_maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(_maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(_maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertDoesNotThrow(() -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertThrows(IllegalArgumentException.class, () -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMinIterations(51)
                .withMaxIterations(50)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

        Assertions.assertDoesNotThrow(() -> MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMinIterations(50)
                .withMaxIterations(50)
                .withTime(maxTime, timeUnit)
                .build(algorithm));

    }

    @Test
    public void checkMinimumAndMaxNumberOfIterations() {
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 80;
        int maxIterWithoutImprovement = 1000;
        int maxTime = 2;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, atLeast(minNumberOfIterations)).algorithm(testInstance);
        verify(algorithm, atMost(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkMinimumStopNotImprovement() {
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 10_000_000;
        int maxTime = 2;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(1)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, times(minNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkMaxNumberOfIterations() {
        int minNumberOfIterations = 1;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;
        int maxTime = 1;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMinIterations(minNumberOfIterations)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm);
        multistart.algorithm(testInstance);
        verify(algorithm, times(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkStopByMaxTime() {
        int maxNumberOfIterations = 10_000_000;
        int maxIterWithoutImprovement = 10_000_000;
        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .withMaxIterations(maxNumberOfIterations)
                .withTime(maxTime, timeUnit)
                .build(algorithm);
        long startTime = System.nanoTime();
        multistart.algorithm(testInstance);
        long endTime = System.nanoTime();
        long ellapsed = (endTime - startTime) / 1_000_000;
        Assertions.assertTrue(ellapsed < 20, "Does not stop after 10 millis");
        Assertions.assertTrue(ellapsed > 5, "Stops in less than 5 millis");
    }

    @Test
    public void checkStopByMaxIterWithoutImprovement() {
        int maxIterWithoutImprovement = 1;
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(maxIterWithoutImprovement)
                .build(algorithm);
        multistart.algorithm(testInstance);
        // Execute twice: First solution is null, improves, second time does not improve, end
        verify(algorithm, times(2)).algorithm(testInstance);
    }

    @Test
    public void checkSetsBuilder(){
        var multistart = MultiStartAlgorithm.<TestSolution, TestInstance>builder()
                .withMaxIterationsWithoutImproving(100)
                .build(algorithm);
        var builder = new ReflectiveSolutionBuilder<TestSolution, TestInstance>();
        verify(algorithm, times(0)).setBuilder(any());
        multistart.setBuilder(builder);
        verify(algorithm, times(1)).setBuilder(any());
    }

}
