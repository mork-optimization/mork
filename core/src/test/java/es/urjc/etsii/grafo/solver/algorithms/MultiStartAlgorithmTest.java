package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class MultiStartAlgorithmTest {

    private Algorithm<TestSolution, TestInstance> algorithm;
    private TestInstance testInstance = new TestInstance("testinstance");
    private TestSolution testSolution = new TestSolution(testInstance);


    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUpAlgorithm(){
        algorithm = Mockito.mock(Algorithm.class);
        when(algorithm.algorithm(testInstance)).thenReturn(testSolution);
    }

    @Test
    public void testIllegalParameters(){
        // Legal
        int minNumberOfIterations = 0;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;
        int maxTime = 1;

        // Illegal
        int _minNumberOfIterations = -1;
        int _maxNumberOfIterations = 0;
        int _maxIterWithoutImprovement = 0;
        int _maxTime = 0;
        TimeUnit timeUnit = TimeUnit.DAYS;
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, _maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, _minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, _maxIterWithoutImprovement, maxTime, timeUnit));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, _maxTime, timeUnit));
        Assertions.assertDoesNotThrow(() -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, 50, 51, maxIterWithoutImprovement, maxTime, timeUnit));
        Assertions.assertDoesNotThrow(() -> new MultiStartAlgorithm<>("TestMultistart", this.algorithm, 50, 50, maxIterWithoutImprovement, maxTime, timeUnit));

    }

    @Test
    public void checkMinimumAndMaxNumberOfIterations(){
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 80;
        int maxIterWithoutImprovement = 1000;
        int maxTime = 2;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit);
        var solution = multistart.algorithm(testInstance);
        verify(algorithm, atLeast(minNumberOfIterations)).algorithm(testInstance);
        verify(algorithm, atMost(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkMinimumStopNotImprovement(){
        int minNumberOfIterations = 30;
        int maxNumberOfIterations = 10_000_000;
        int maxIterWithoutImprovement = 1;
        int maxTime = 2;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit);
        var solution = multistart.algorithm(testInstance);
        verify(algorithm, times(minNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkMaxNumberOfIterations(){
        int minNumberOfIterations = 0;
        int maxNumberOfIterations = 100;
        int maxIterWithoutImprovement = 100;
        int maxTime = 1;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit);
        var solution = multistart.algorithm(testInstance);
        verify(algorithm, times(maxNumberOfIterations)).algorithm(testInstance);
    }

    @Test
    public void checkStopByMaxTime(){
        int minNumberOfIterations = 0;
        int maxNumberOfIterations = 10_000_000;
        int maxIterWithoutImprovement = 10_000_000;
        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        var multistart = new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit);
        long startTime = System.nanoTime();
        var tempsolution = multistart.algorithm(testInstance);
        long endTime = System.nanoTime();
        long ellapsed = (endTime - startTime) / 1_000_000;
        Assertions.assertTrue(ellapsed < 15, "Does not stop after 10 millis");
        Assertions.assertTrue(ellapsed > 5, "Stops in less than 5 millis");
    }

    @Test
    public void checkStopByMaxIterWithoutImprovement(){
        int minNumberOfIterations = 0;
        int maxNumberOfIterations = 1_000_000;
        int maxIterWithoutImprovement = 1;
        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.DAYS;
        var multistart = new MultiStartAlgorithm<>("TestMultistart", this.algorithm, maxNumberOfIterations, minNumberOfIterations, maxIterWithoutImprovement, maxTime, timeUnit);
        var solution = multistart.algorithm(testInstance);
        // Execute twice: First solution is null, improves, second time does not improve, end
        verify(algorithm, times(2)).algorithm(testInstance);
    }


}
