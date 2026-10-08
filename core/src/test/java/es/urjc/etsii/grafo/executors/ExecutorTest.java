package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.PingEvent;
import es.urjc.etsii.grafo.exception.ExceptionHandler;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
    MorkEventPublisher eventPublisher;
    ResultStore<TestSolution, TestInstance> resultStore;
    ResultsSerializerListener<TestSolution, TestInstance> resultsSerializer;

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
        this.eventPublisher = mock(MorkEventPublisher.class);
        when(this.eventPublisher.mute()).thenReturn(() -> {});
        this.resultStore = new ResultStore<>();
        this.resultsSerializer = mock(ResultsSerializerListener.class);

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
                referenceResultManager,
                eventPublisher,
                resultStore,
                resultsSerializer);
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

    @Test
    public void warmupRunsAlgorithmsWithoutExportingResults(){
        var warmupInstance = new TestInstance("warmup");
        when(instanceManager.getInstance("warmup")).thenReturn(warmupInstance);

        var solverConfig = new SolverConfig();
        solverConfig.setRandomType(RandomType.DEFAULT);
        solverConfig.getWarmup().setEnabled(true);
        solverConfig.getWarmup().setRepetitions(2);

        var warmupExecutor = new TestExecutor(
                Optional.of(this.validator),
                Optional.of(this.timeLimitCalculator),
                this.ioManager,
                this.instanceManager,
                solverConfig,
                List.of(new NopExceptionHandler()),
                referenceResultManager,
                eventPublisher,
                resultStore,
                resultsSerializer);
        var algorithm = new CountingAlgorithm("warmupAlgorithm");
        var experiment = new Experiment<>("WarmupExperiment", ExecutorTest.class, List.of(algorithm));

        warmupExecutor.warmup(experiment, "warmup");

        assertEquals(2, algorithm.calls.get());
        verify(ioManager, never()).exportSolution(any(), any());
    }

    @Test
    public void warmupBlocksEventsPublishedByAlgorithm(){
        var listener = mock(MorkEventListener.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var eventPublisher = new MorkEventPublisher(List.of(listener), messagingTemplate, eventLog);
        try {
            var warmupInstance = new TestInstance("warmup");
            when(instanceManager.getInstance("warmup")).thenReturn(warmupInstance);

            var solverConfig = new SolverConfig();
            solverConfig.setRandomType(RandomType.DEFAULT);
            solverConfig.getWarmup().setEnabled(true);

            var warmupExecutor = new TestExecutor(
                    Optional.of(this.validator),
                    Optional.of(this.timeLimitCalculator),
                    this.ioManager,
                    this.instanceManager,
                    solverConfig,
                    List.of(new NopExceptionHandler()),
                    referenceResultManager,
                    eventPublisher,
                    new ResultStore<>(),
                    resultsSerializer);
            var experiment = new Experiment<>("WarmupExperiment", ExecutorTest.class, List.of(new EventPublishingAlgorithm(eventPublisher)));

            warmupExecutor.warmup(experiment, "warmup");

            verify(listener, after(100).never()).onEvent(isA(PingEvent.class));
        } finally {
            eventPublisher.destroy();
        }
    }

    @Test
    void perInstanceSerializationCompletesBeforeEndingEvent() {
        ((TestExecutor) executor).finishInstanceForTest("exp", "instance", 10L, 20L);

        var ordered = inOrder(resultsSerializer, eventPublisher);
        ordered.verify(resultsSerializer).serializePerInstance("exp", 20L);
        ordered.verify(eventPublisher).publish(isA(InstanceProcessingEndedEvent.class));
    }

    @Test
    void perInstanceSerializationFailureIsReportedAndEndingEventStillPublishes() {
        doThrow(new IllegalStateException("serializer failed"))
                .when(resultsSerializer).serializePerInstance("exp", 20L);

        assertDoesNotThrow(() -> ((TestExecutor) executor).finishInstanceForTest("exp", "instance", 10L, 20L));

        var ordered = inOrder(resultsSerializer, eventPublisher);
        ordered.verify(resultsSerializer).serializePerInstance("exp", 20L);
        ordered.verify(eventPublisher).publish(isA(ErrorEvent.class));
        ordered.verify(eventPublisher).publish(isA(InstanceProcessingEndedEvent.class));
    }

    @Test
    void bestResultSelectionUsesCachedObjectiveValues() {
        var evaluations = new AtomicInteger();
        Context.Configurator.setObjectives(Objective.of(
                "Counting",
                FMode.MINIMIZE,
                solution -> {
                    evaluations.incrementAndGet();
                    return solution.getScore();
                },
                TestMove::getScoreChange
        ));
        var algorithm = new CountingAlgorithm("algorithm");
        var workUnit = new WorkUnit<>("exp", "inst1", algorithm, 0);
        var candidateSolution = new TestSolution(new TestInstance("inst1"), 1.0);
        var bestSolution = new TestSolution(new TestInstance("inst1"), 2.0);
        var candidate = WorkUnitResult.ok(workUnit, "inst1", candidateSolution, 1L, 1L, null, List.of());
        var best = WorkUnitResult.ok(workUnit, "inst1", bestSolution, 1L, 1L, null, List.of());
        assertEquals(2, evaluations.get());

        candidateSolution.setScore(100.0);
        bestSolution.setScore(0.0);

        assertTrue(((TestExecutor) executor).improvesForTest(candidate, best));
        assertEquals(2, evaluations.get());
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
                ReferenceResultManager referenceResultManager,
                MorkEventPublisher eventPublisher,
                ResultStore<TestSolution, TestInstance> resultStore,
                ResultsSerializerListener<TestSolution, TestInstance> resultsSerializer

        ) {
            super(solutionValidator, timeLimitCalculator,
                    io, instanceManager, solverConfig, exceptionHandlers, referenceResultManager, eventPublisher, resultStore, resultsSerializer);
        }

        @Override
        public void executeExperiment(Experiment<TestSolution, TestInstance> experiment, List<String> instancePaths, long startTimestamp) {

        }

        @Override
        public void startup() {
            // No resources to initialize
        }

        @Override
        public void shutdown() {
            // No resources to clean
        }

        private void finishInstanceForTest(String experimentName, String instanceName, long executionTime, long startTimestamp) {
            finishInstance(experimentName, instanceName, executionTime, startTimestamp);
        }

        private boolean improvesForTest(
                WorkUnitResult<TestSolution, TestInstance> candidate,
                WorkUnitResult<TestSolution, TestInstance> best
        ) {
            return improves(candidate, best);
        }
    }

    private static class CountingAlgorithm extends Algorithm<TestSolution, TestInstance> {
        private final AtomicInteger calls = new AtomicInteger();

        private CountingAlgorithm(String algorithmName) {
            super(algorithmName);
        }

        @Override
        public TestSolution algorithm(TestInstance instance) {
            calls.incrementAndGet();
            var solution = new TestSolution(instance, 8.0);
            solution.notifyUpdate();
            return solution;
        }
    }

    private static class EventPublishingAlgorithm extends Algorithm<TestSolution, TestInstance> {
        private final MorkEventPublisher eventPublisher;

        private EventPublishingAlgorithm(MorkEventPublisher eventPublisher) {
            super("eventPublishingAlgorithm");
            this.eventPublisher = eventPublisher;
        }

        @Override
        public TestSolution algorithm(TestInstance instance) {
            eventPublisher.publish(new PingEvent());
            var solution = new TestSolution(instance, 8.0);
            solution.notifyUpdate();
            return solution;
        }
    }
}
