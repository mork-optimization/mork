package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.exception.ResourceLimitException;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.experiment.ExperimentManager;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializerListener;
import es.urjc.etsii.grafo.orchestrator.DefaultOrchestrator;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultOrchestratorTest {

    @Test
    void verifyWorkloadLimits(){
        var t1 = getTestData(100, 100, 100);
        Assertions.assertThrows(ResourceLimitException.class, () -> DefaultOrchestrator.verifyWorkloadLimit(t1.config(), t1.instances(), t1.algorithms()));

        var t2 = getTestData(1000, 1000, 1000);
        Assertions.assertThrows(ResourceLimitException.class, () -> DefaultOrchestrator.verifyWorkloadLimit(t2.config(), t2.instances(), t2.algorithms()));

        var t3 = getTestData(90, 90, 90);
        Assertions.assertDoesNotThrow(() -> DefaultOrchestrator.verifyWorkloadLimit(t3.config(), t3.instances(), t3.algorithms()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void finalSerializationFailureIsReportedBeforeExperimentEndAndLifecycleStillCompletes() {
        Context.Configurator.setObjectives(Objective.of(
                "Test",
                FMode.MINIMIZE,
                TestSolution::getScore,
                TestMove::getScoreChange
        ));
        var solverConfig = new SolverConfig();
        var instanceManager = (InstanceManager<TestInstance>) mock(InstanceManager.class);
        var experimentManager = (ExperimentManager<TestSolution, TestInstance>) mock(ExperimentManager.class);
        var executor = (Executor<TestSolution, TestInstance>) mock(Executor.class);
        var eventPublisher = mock(MorkEventPublisher.class);
        var lifecycle = mock(ExecutionLifecycleCoordinator.class);
        var resultsSerializer = (ResultsSerializerListener<TestSolution, TestInstance>) mock(ResultsSerializerListener.class);
        var experiment = new Experiment<TestSolution, TestInstance>("experiment", getClass(), List.of());
        when(experimentManager.getExperiments()).thenReturn(Map.of(experiment.name(), experiment));
        when(instanceManager.getInstanceSolveOrder(experiment.name())).thenReturn(List.of("instance"));
        // The timestamp is generated inside the orchestrator, so match it independently below.
        doThrow(new IllegalStateException("serializer failed"))
                .when(resultsSerializer).serializeAtExperimentEnd(org.mockito.ArgumentMatchers.eq(experiment.name()), anyLong());

        var orchestrator = new DefaultOrchestrator<>(
                solverConfig,
                new BlockConfig(),
                instanceManager,
                experimentManager,
                Optional.empty(),
                executor,
                eventPublisher,
                lifecycle,
                resultsSerializer
        );

        Assertions.assertDoesNotThrow(() -> orchestrator.run());

        var ordered = inOrder(executor, resultsSerializer, eventPublisher, lifecycle);
        ordered.verify(executor).executeExperiment(org.mockito.ArgumentMatchers.same(experiment), org.mockito.ArgumentMatchers.anyList(), anyLong());
        ordered.verify(resultsSerializer).serializeAtExperimentEnd(org.mockito.ArgumentMatchers.eq(experiment.name()), anyLong());
        ordered.verify(eventPublisher).publish(isA(ErrorEvent.class));
        ordered.verify(eventPublisher).publish(isA(ExperimentEndedEvent.class));
        ordered.verify(executor).shutdown();
        ordered.verify(lifecycle).complete(anyLong());
        verify(eventPublisher, never()).publish(isA(ExecutionEndedEvent.class));
    }

    private TestData getTestData(int repetitions, int nInstances, int nAlgorithms){
        List<Algorithm<TestSolution, TestInstance>> algorithms = new ArrayList<>();
        for (int i = 0; i < nAlgorithms; i++) {
            algorithms.add(new SimpleAlgorithm<>("Test-"+i, new NullConstructive(), Improver.nul()));
        }

        List<String> instances = new ArrayList<>();
        for (int i = 0; i < nInstances; i++) {
            instances.add("name-" + i);
        }

        SolverConfig config = new SolverConfig();
        config.setRepetitions(repetitions);
        return new TestData(config, instances, algorithms);
    }

    private record TestData(SolverConfig config, List<String> instances, List<Algorithm<TestSolution, TestInstance>> algorithms){}

    private static class NullConstructive extends Constructive<TestSolution, TestInstance> {
        @Override
        public TestSolution construct(TestSolution solution) {
            return solution;
        }
    }
}
