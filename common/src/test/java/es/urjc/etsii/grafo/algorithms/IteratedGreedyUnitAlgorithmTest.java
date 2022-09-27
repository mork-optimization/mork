package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.NullConstructive;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class IteratedGreedyUnitAlgorithmTest {

    private final TestInstance testInstance = new TestInstance("testinstance");
    private final TestSolution testSolution = new TestSolution(testInstance);
    private Shake<TestSolution, TestInstance> mockitoShake;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUpAlgorithm() {
        mockitoShake = Mockito.mock(Shake.class);
        when(mockitoShake.shake(any(TestSolution.class), anyInt())).thenReturn(testSolution);
    }

    @Test
    void testIllegalParameters() {
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Improver<TestSolution, TestInstance> improver = new Improver.NullImprover<>();

        // Legal
        int maxIterations = 10;
        int stopIfNotImprovedIn = 5;

        // Illegal
        int _maxIterations = -1;
        int _stopIfNotImprovedIn = 0;

        Assertions.assertDoesNotThrow(() ->
                new IteratedGreedy<>(maxIterations, stopIfNotImprovedIn, constructive, mockitoShake, improver));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new IteratedGreedy<>(_maxIterations, stopIfNotImprovedIn, constructive, mockitoShake, improver));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new IteratedGreedy<>(maxIterations, _stopIfNotImprovedIn, constructive, mockitoShake, improver));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new IteratedGreedy<>(_maxIterations, _stopIfNotImprovedIn, constructive, mockitoShake, improver));
    }

    @Test
    void checkMinimumAndMaxNumberOfIterations() {
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Improver<TestSolution, TestInstance> improver = new Improver.NullImprover<>();
        int maxIterations = 10;
        int stopIfNotImprovedIn = 5;
        IteratedGreedy<TestSolution, TestInstance> iteratedGreedy = new IteratedGreedy<>(maxIterations, stopIfNotImprovedIn, constructive, mockitoShake, improver);
        iteratedGreedy.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
        iteratedGreedy.algorithm(testInstance);
        verify(mockitoShake, atLeast(stopIfNotImprovedIn)).shake(any(TestSolution.class), anyInt());
        verify(mockitoShake, atMost(maxIterations)).shake(any(TestSolution.class), anyInt());
    }

    @Test
    void checkMinimumStopNotImprovement() {
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Improver<TestSolution, TestInstance> improver = new Improver.NullImprover<>();
        int maxIterations = 10_000_000;
        int stopIfNotImprovedIn = 10;
        IteratedGreedy<TestSolution, TestInstance> iteratedGreedy = new IteratedGreedy<>(maxIterations, stopIfNotImprovedIn, constructive, mockitoShake, improver);
        iteratedGreedy.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
        iteratedGreedy.algorithm(testInstance);
        verify(mockitoShake, times(stopIfNotImprovedIn)).shake(any(TestSolution.class), anyInt());
    }

    @Test
    void checkMaxNumberOfIterations() {
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Improver<TestSolution, TestInstance> improver = new Improver.NullImprover<>();
        int maxIterations = 10;
        int stopIfNotImprovedIn = 10_000_000;
        IteratedGreedy<TestSolution, TestInstance> iteratedGreedy = new IteratedGreedy<>(maxIterations, stopIfNotImprovedIn, constructive, mockitoShake, improver);
        iteratedGreedy.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
        iteratedGreedy.algorithm(testInstance);
        verify(mockitoShake, times(maxIterations)).shake(any(TestSolution.class), anyInt());
    }

    @Test
    void checkStopByMaxTime() {
        Constructive<TestSolution, TestInstance> constructive = new NullConstructive<>();
        Shake<TestSolution, TestInstance> shake = new Shake.NullShake<>();
        Improver<TestSolution, TestInstance> improver = new Improver.NullImprover<>();
        int maxIterations = 10_000_000;
        int stopIfNotImprovedIn = 10_000_000;

        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        TimeControl.setMaxExecutionTime(maxTime, timeUnit);
        TimeControl.start();
        IteratedGreedy<TestSolution, TestInstance> iteratedGreedy = new IteratedGreedy<>(maxIterations, stopIfNotImprovedIn, constructive, shake, improver);
        iteratedGreedy.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });

        long startTime = System.nanoTime();
        iteratedGreedy.algorithm(testInstance);
        long endTime = System.nanoTime();
        long ellapsed = (endTime - startTime) / 1_000_000;
        TimeControl.remove();
        Assertions.assertTrue(ellapsed < 20, "Does not stop after 10 millis");
        Assertions.assertTrue(ellapsed > 5, "Stops in less than 5 millis");
    }

}
