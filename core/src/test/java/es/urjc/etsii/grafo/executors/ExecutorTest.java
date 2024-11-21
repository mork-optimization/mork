package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ExecutorTest {
    private static final Objective<TestMove, TestSolution, TestInstance> OBJ_MIN = Objective.of("Test", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);
    private ReferenceResultManager referenceResultManager;

    private static class NopExceptionHandler extends ExceptionHandler<TestSolution, TestInstance> {

        @Override
        public void handleException(String experimentName, int iteration, Exception e, Optional<TestSolution> testSolution, TestInstance testInstance, Algorithm<TestSolution, TestInstance> algorithm) {
        }
    }

    ReferenceResultProvider referenceResultProvider;
    InstanceManager<TestInstance> instanceManager;
    SolutionValidator<TestSolution, TestInstance> validator;
    TimeLimitCalculator<TestSolution, TestInstance> timeLimitCalculator;
    IOManager<TestSolution, TestInstance> ioManager;

    Executor<TestSolution, TestInstance> executor;

    @BeforeEach
    void initMocks(){
        Context.Configurator.setObjectives(OBJ_MIN);
        var instance1 = new TestInstance("inst1");
        var referenceResult1 = new ReferenceResult();
        referenceResult1.addScores(Map.of("Test", 5.0));
        referenceResult1.setOptimalValue(true);

        var referenceResult2 = new ReferenceResult();
        referenceResult2.addScores(Map.of("Test", 7.0));
        referenceResult2.setOptimalValue(false);

        this.referenceResultProvider = mock(ReferenceResultProvider.class);
        when(referenceResultProvider.getProviderName()).thenReturn("MockedProvider");
        when(referenceResultProvider.getValueFor("inst1")).thenReturn(referenceResult1);
        when(referenceResultProvider.getValueFor("inst2")).thenReturn(referenceResult2);

        this.instanceManager = mock(InstanceManager.class);
        when(instanceManager.getInstance("inst1")).thenReturn(instance1);

        this.timeLimitCalculator = mock(TimeLimitCalculator.class);
        when(timeLimitCalculator.timeLimitInMillis(any(TestInstance.class), any(Algorithm.class))).thenReturn(1_000L);

        this.ioManager = mock(IOManager.class);

        this.referenceResultManager = new ReferenceResultManager(List.of(this.referenceResultProvider));
        Context.Configurator.setRefResultManager(referenceResultManager);

        this.validator = mock(SolutionValidator.class);
        when(validator.validate(any(TestSolution.class))).thenReturn(ValidationResult.ok());
        Context.Configurator.setValidator(this.validator);

        this.executor = new TestExecutor(
                Optional.of(this.validator),
                Optional.of(this.timeLimitCalculator),
                this.ioManager,
                this.instanceManager,
                new SolverConfig(), // Not relevant for this test? leave with default values
                List.of(new NopExceptionHandler()),
                referenceResultManager);
    }

    @Test
    public void testGetOptionalReferenceResult(){
        var refValues = referenceResultManager.getRefValueForAllObjectives("inst1", true);
        assertTrue(refValues.containsKey("Test"));
        assertEquals(5.0, refValues.get("Test"));

        refValues = referenceResultManager.getRefValueForAllObjectives("inst2", true);
        assertFalse(refValues.containsKey("Test"));

        refValues = referenceResultManager.getRefValueForAllObjectives("inst2", false);
        assertTrue(refValues.containsKey("Test"));
        assertEquals(7.0, refValues.get("Test"));
    }

    @Test
    public void testValidateScore(){
        var solution = new TestSolution(new TestInstance("inst1"));
        solution.notifyUpdate();

        solution.setScore(4.0); // Improves optimal value
        assertThrows(AssertionError.class, () -> Context.validate(solution));

        solution.setScore(8.0);
        assertDoesNotThrow(() -> Context.validate(solution));
        verify(this.validator, atLeastOnce()).validate(solution);;
    }

    @Test
    public void testValidateTTB(){
        var solution = new TestSolution(new TestInstance("inst1"));

        solution.setScore(10.0);
        assertThrows(AssertionError.class, () -> Context.validate(solution)); // No TTB

        solution.notifyUpdate();
        assertDoesNotThrow(() -> Context.validate(solution));
        verify(this.validator, atLeastOnce()).validate(solution);
    }




    public static class TestExecutor extends Executor<TestSolution, TestInstance>{
        /**
         * Fill common values used by all executors
         * @param solutionValidator   solution validator if available
         * @param timeLimitCalculator time limit calculator if exists
         * @param io                                          IO manager
         * @param instanceManager   instance manager
         * @param referenceResultManager manages all reference value providers implementations
         * @param solverConfig  solver configuration
         */
        protected TestExecutor(
                Optional<SolutionValidator<TestSolution, TestInstance>> solutionValidator,
                Optional<TimeLimitCalculator<TestSolution, TestInstance>> timeLimitCalculator,
                IOManager<TestSolution, TestInstance> io, InstanceManager<TestInstance> instanceManager,
                SolverConfig solverConfig,
                List<ExceptionHandler<TestSolution, TestInstance>> exceptionHandlers,
                ReferenceResultManager referenceResultManager

        ) {
            super(solutionValidator, timeLimitCalculator,
                    io, instanceManager, solverConfig, exceptionHandlers, referenceResultManager);
        }

        @Override
        public void executeExperiment(Experiment<TestSolution, TestInstance> experiment, List<String> instanceNames, long startTimestamp) {

        }

        @Override
        public void startup() {
            // No resources to initialize
        }

        @Override
        public void shutdown() {
            // No resources to clean
        }
    }
}