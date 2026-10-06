package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.metrics.timing.TimeStatsService;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConcurrentMonitoringTest {
    @Test
    @SuppressWarnings("unchecked")
    void laterCompletionIsVisibleWhileTheFirstExportIsBlocked() throws Exception {
        Context.Configurator.setObjectives(Objective.<TestMove, TestSolution, TestInstance>of("Cost", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange));
        var validator = (SolutionValidator<TestSolution, TestInstance>) mock(SolutionValidator.class);
        when(validator.validate(any())).thenReturn(ValidationResult.ok());
        var slowStarted = new CountDownLatch(1);
        var releaseSlow = new CountDownLatch(1);
        var fastPublished = new CountDownLatch(1);
        var calls = new AtomicInteger();
        var algorithm = new Algorithm<TestSolution, TestInstance>("algorithm") {
            @Override public TestSolution algorithm(TestInstance instance) {
                if (calls.getAndIncrement() == 0) {
                    slowStarted.countDown();
                    try { if (!releaseSlow.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Timed out"); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                } else {
                    try { assertTrue(slowStarted.await(5, TimeUnit.SECONDS)); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                }
                var solution = new TestSolution(instance, 5); solution.notifyUpdate(); return solution;
            }
        };
        var config = new SolverConfig(); config.setRepetitions(2); config.setnWorkers(2); config.setProgressBar(false);
        config.setRandomType(es.urjc.etsii.grafo.util.random.RandomType.DEFAULT);
        var results = new ResultStore<TestSolution, TestInstance>();
        var state = new NormalExperimentRunState<TestSolution, TestInstance>(results, mock(ObjectProvider.class));
        var experiment = new Experiment<>("experiment", getClass(), List.of(algorithm));
        state.prepare(Map.of("Cost", FMode.MINIMIZE)); state.plan(experiment, List.of("instance"), 2, config.getSeed());
        var manager = (InstanceManager<TestInstance>) mock(InstanceManager.class);
        when(manager.getInstance("instance")).thenReturn(new TestInstance("instance"));
        var publisher = mock(MorkEventPublisher.class);
        doAnswer(invocation -> { if (invocation.getArgument(0) instanceof SolutionGeneratedEvent) fastPublished.countDown(); return null; }).when(publisher).publish(any());
        var io = (IOManager<TestSolution, TestInstance>) mock(IOManager.class);
        var executor = new ConcurrentExecutor<>(config, Optional.of(validator), Optional.empty(), io, manager, List.of(new es.urjc.etsii.grafo.exceptions.DefaultExceptionHandler<>(io)),
                new ReferenceResultManager(List.of()), publisher, results, mock(ResultsSerializerListener.class), mock(TimeStatsService.class), state);
        executor.startup();
        try (var caller = Executors.newSingleThreadExecutor()) {
            var execution = caller.submit(() -> executor.executeExperiment(experiment, List.of("instance"), System.nanoTime()));
            try {
                if (!fastPublished.await(5, TimeUnit.SECONDS)) {
                    if (execution.isDone()) execution.get();
                    fail("A completed repetition was not published while its predecessor ran");
                }
                assertEquals(1, state.status().counts().succeeded()); assertEquals(1, state.status().counts().running());
                assertEquals(1, results.getResultsForExperiment("experiment").size());
                verifyNoInteractions(io);
            } finally { releaseSlow.countDown(); }
            execution.get(5, TimeUnit.SECONDS);
            assertEquals(2, state.status().counts().succeeded()); assertEquals(1, state.status().completedInstances());
        } finally { releaseSlow.countDown(); executor.shutdown(); }
    }
}
