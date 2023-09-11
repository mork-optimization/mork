package es.urjc.etsii.grafo.improve.sa;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.improve.sa.cd.CoolDownControl;
import es.urjc.etsii.grafo.improve.sa.cd.ExponentialCoolDown;
import es.urjc.etsii.grafo.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.improve.sa.initialt.MaxDifferenceInitialTemperature;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestNeighborhood;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class SimulatedAnnealingUnitAlgorithmTest {

    private final TestInstance testInstance = new TestInstance("testinstance");
    private final TestSolution testSolution = new TestSolution(testInstance);
    Constructive<TestSolution, TestInstance> constructive = Constructive.nul();
    private CoolDownControl mockitoCoolDownControl;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUpAlgorithm() {
        mockitoCoolDownControl = Mockito.mock(ExponentialCoolDown.class);
        when(mockitoCoolDownControl.coolDown(any(TestSolution.class), any(Neighborhood.class), anyDouble(), anyInt())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (double) args[2] * 0.8;
            }
        });
    }

    @Test
    void testIllegalParameters() {
        CoolDownControl coolDownControl = new ExponentialCoolDown<>(0.5);
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>();
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> iteration > 15;
        InitialTemperatureCalculator initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>();
        RandomizableNeighborhood randomizableNeighborhood = new Neighborhood.EmptyNeighborhood<>();

        Assertions.assertDoesNotThrow(() ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(-1)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .build());

        Assertions.assertDoesNotThrow(() ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

    }

    @Test
    void checkMinimumAndMaxNumberOfIterations() {
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>();
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> iteration >= 15;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        RandomizableNeighborhood randomizableNeighborhood = new TestNeighborhood(testSolution, 0.5, 0.5);


        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(mockitoCoolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                .withMode(FMode.MAXIMIZE)
                        .withNeighborhood(randomizableNeighborhood)
                        .build();
        SimpleAlgorithm simpleAlgorithm = new SimpleAlgorithm<>("Test", constructive, sa);
        simpleAlgorithm.setBuilder(new SolutionBuilder() {
            @Override
            public Solution initializeSolution(Instance instance) {
                return new TestSolution((TestInstance) instance);
            }
        });
        simpleAlgorithm.algorithm(testInstance);
        verify(mockitoCoolDownControl, atLeast(1)).coolDown(any(TestSolution.class), any(Neighborhood.class), anyDouble(), anyInt());
        verify(mockitoCoolDownControl, atMost(15)).coolDown(any(TestSolution.class), any(Neighborhood.class), anyDouble(), anyInt());
    }

    @Test
    void checkMinimumAndMaxNumberOfIterationsByTemperature() {
        mockitoCoolDownControl = Mockito.mock(ExponentialCoolDown.class);
        when(mockitoCoolDownControl.coolDown(any(TestSolution.class), any(Neighborhood.class), anyDouble(), anyInt())).thenReturn(0.0);
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>();
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> currentTemperature <= 0.0;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        RandomizableNeighborhood randomizableNeighborhood = new TestNeighborhood(testSolution, 0.5, 0.5);


        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                .withCoolDownCustom(mockitoCoolDownControl)
                .withAcceptanceCriteriaCustom(acceptanceCriteria)
                .withCycleLength(10)
                .withTerminationCriteriaCustom(terminationCriteria)
                .withInitialTempFunction(initialTemperatureCalculator)
                .withMode(FMode.MAXIMIZE)
                .withNeighborhood(randomizableNeighborhood)
                .build();
        SimpleAlgorithm simpleAlgorithm = new SimpleAlgorithm<>("Test", constructive, sa);
        simpleAlgorithm.setBuilder(new SolutionBuilder() {
            @Override
            public Solution initializeSolution(Instance instance) {
                return new TestSolution((TestInstance) instance);
            }
        });
        simpleAlgorithm.algorithm(testInstance);
        verify(mockitoCoolDownControl, times(1)).coolDown(any(TestSolution.class), any(Neighborhood.class), anyDouble(), anyInt());
    }

    @Test
    void checkStopByMaxTime() {
        CoolDownControl coolDownControl = new ExponentialCoolDown<>(0.99);
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>();
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> iteration == Integer.MAX_VALUE;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        RandomizableNeighborhood randomizableNeighborhood = new TestNeighborhood(testSolution, 0.5, 0.5);
        int maxTime = 10;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        TimeControl.setMaxExecutionTime(maxTime, timeUnit);
        TimeControl.start();
        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                .withCoolDownCustom(coolDownControl)
                .withAcceptanceCriteriaCustom(acceptanceCriteria)
                .withCycleLength(10)
                .withTerminationCriteriaCustom(terminationCriteria)
                .withInitialTempFunction(initialTemperatureCalculator)
                .withMode(FMode.MAXIMIZE)
                .withNeighborhood(randomizableNeighborhood)
                .build();
        SimpleAlgorithm simpleAlgorithm = new SimpleAlgorithm<>("Test", constructive, sa);
        simpleAlgorithm.setBuilder(new SolutionBuilder<TestSolution, TestInstance>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
        long startTime = System.nanoTime();
        simpleAlgorithm.algorithm(testInstance);
        long endTime = System.nanoTime();
        long ellapsed = (endTime - startTime) / 1_000_000;
        TimeControl.remove();
        // TODO: SimpleAlgorithm no comprueba el tiempo, ni tampoco sa
        Assertions.assertTrue(ellapsed < 20, "Does not stop after 10 millis");
        Assertions.assertTrue(ellapsed > 5, "Stops in less than 5 millis");
    }

}
