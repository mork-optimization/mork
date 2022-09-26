package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.exception.ResourceLimitException;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.orchestrator.UserExperimentOrchestrator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class UserExperimentOrchestratorTest {

    @Test
    void verifyWorkloadLimits(){
        var t1 = getTestData(100, 100, 100);
        Assertions.assertThrows(ResourceLimitException.class, () -> UserExperimentOrchestrator.verifyWorkloadLimit(t1.config(), t1.instances(), t1.algorithms()));

        var t2 = getTestData(1000, 1000, 1000);
        Assertions.assertThrows(ResourceLimitException.class, () -> UserExperimentOrchestrator.verifyWorkloadLimit(t2.config(), t2.instances(), t2.algorithms()));

        var t3 = getTestData(90, 90, 90);
        Assertions.assertDoesNotThrow(() -> UserExperimentOrchestrator.verifyWorkloadLimit(t3.config(), t3.instances(), t3.algorithms()));
    }

    private TestData getTestData(int repetitions, int nInstances, int nAlgorithms){
        List<Algorithm<TestSolution, TestInstance>> algorithms = new ArrayList<>();
        for (int i = 0; i < nAlgorithms; i++) {
            algorithms.add(new SimpleAlgorithm<>(new NullConstructive(), Improver.nul()));
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
