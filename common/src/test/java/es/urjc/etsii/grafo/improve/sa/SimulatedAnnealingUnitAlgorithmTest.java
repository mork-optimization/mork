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
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.testutil.*;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;


@SuppressWarnings({"rawtypes", "unchecked"})
class SimulatedAnnealingUnitAlgorithmTest {

    private final TestInstance testInstance = new TestInstance("testinstance");
    private final TestSolution testSolution = new TestSolution(testInstance);
    private static final Objective<?,?,?> objective = Objective.of("Test", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);

    Constructive<TestSolution, TestInstance> constructive = Constructive.nul();
    private CoolDownControl mockitoCoolDownControl;

    @BeforeAll
    public static void init(){
        Context.reset();
        Context.Configurator.setObjectives(objective);
        Metrics.disableMetrics();
    }

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
        Context.Configurator.resetRandom(RandomType.DEFAULT, 1234);
    }

    @Test
    void testIllegalParameters() {
        CoolDownControl coolDownControl = new ExponentialCoolDown<>(0.5);
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>(objective);
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> iteration > 15;
        InitialTemperatureCalculator initialTemperatureCalculator = new MaxDifferenceInitialTemperature<>(objective);
        RandomizableNeighborhood randomizableNeighborhood = new Neighborhood.EmptyNeighborhood<>();

        Assertions.assertDoesNotThrow(() ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

        Assertions.assertThrows(IllegalArgumentException.class, () -> new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(-1)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

        var sa = new SimulatedAnnealingBuilder<>()
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build();
        Assertions.assertInstanceOf(ExponentialCoolDown.class, sa.coolDownControl);


        sa = new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build();
        Assertions.assertInstanceOf(MetropolisAcceptanceCriteria.class, sa.acceptanceCriteria);

        sa = new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build();

        var fakeObj = Objective.of("Fake", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);
        Context.Configurator.setObjectives(fakeObj);
        sa = new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withNeighborhood(randomizableNeighborhood)
                        .build();
        Assertions.assertEquals(fakeObj, sa.objective);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .build());

        Assertions.assertDoesNotThrow(() ->
                new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(coolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                        .withObjective(objective)
                        .withNeighborhood(randomizableNeighborhood)
                        .build());

    }

    @Test
    void checkMinimumAndMaxNumberOfIterations() {
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>(objective);
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> iteration >= 15;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        RandomizableNeighborhood randomizableNeighborhood = new TestNeighborhood(testSolution, 0.5, 0.5);


        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                        .withCoolDownCustom(mockitoCoolDownControl)
                        .withAcceptanceCriteriaCustom(acceptanceCriteria)
                        .withCycleLength(10)
                        .withTerminationCriteriaCustom(terminationCriteria)
                        .withInitialTempFunction(initialTemperatureCalculator)
                .withObjective(objective)
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
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>(objective);
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> currentTemperature <= 0.0;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        RandomizableNeighborhood randomizableNeighborhood = new TestNeighborhood(testSolution, 0.5, 0.5);


        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                .withCoolDownCustom(mockitoCoolDownControl)
                .withAcceptanceCriteriaCustom(acceptanceCriteria)
                .withCycleLength(10)
                .withTerminationCriteriaCustom(terminationCriteria)
                .withInitialTempFunction(initialTemperatureCalculator)
                .withObjective(objective)
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkStopByMaxTime(boolean expiredBeforeStart) {
        var timeUp = new AtomicBoolean(expiredBeforeStart);
        CoolDownControl coolDownControl = new ExponentialCoolDown<>(0.99);
        AcceptanceCriteria acceptanceCriteria = new MetropolisAcceptanceCriteria<>(objective);
        TerminationCriteria terminationCriteria = (solution, neighborhood, currentTemperature, iteration) -> false;
        InitialTemperatureCalculator initialTemperatureCalculator = (ignored1, ignored2) -> Integer.MAX_VALUE;
        var randomizableNeighborhood = spy(new TestNeighborhood(testSolution, -0.5));
        doAnswer(invocation -> {
            Assertions.assertFalse(timeUp.get(), "Must not request another move after the deadline expires");
            timeUp.set(true);
            return invocation.callRealMethod();
        }).when(randomizableNeighborhood).getRandomMove(any(TestSolution.class));

        SimulatedAnnealing sa = new SimulatedAnnealingBuilder<>()
                .withCoolDownCustom(coolDownControl)
                .withAcceptanceCriteriaCustom(acceptanceCriteria)
                .withCycleLength(10)
                .withTerminationCriteriaCustom(terminationCriteria)
                .withInitialTempFunction(initialTemperatureCalculator)
                .withObjective(objective)
                .withNeighborhood(randomizableNeighborhood)
                .build();
        try (var timeControl = mockStatic(TimeControl.class)) {
            timeControl.when(TimeControl::isTimeUp).thenAnswer(invocation -> timeUp.get());

            var result = (TestSolution) sa.improve(testSolution);

            Assertions.assertEquals(expiredBeforeStart ? 0.0 : -0.5, result.getScore());
            verify(randomizableNeighborhood, times(expiredBeforeStart ? 0 : 1)).getRandomMove(any(TestSolution.class));
        }
    }

}
