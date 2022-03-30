package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.NoOp;
import es.urjc.etsii.grafo.solver.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.exception.ResourceLimitException;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorTest {

    @Test
    public void verifyWorkloadLimits(){
        var t1 = getTestData(100, 100, 100);
        Assertions.assertThrows(ResourceLimitException.class, () -> Orchestrator.verifyWorkloadLimit(t1.config(), t1.instances(), t1.algorithms()));

        var t2 = getTestData(1000, 1000, 1000);
        Assertions.assertThrows(ResourceLimitException.class, () -> Orchestrator.verifyWorkloadLimit(t2.config(), t2.instances(), t2.algorithms()));

        var t3 = getTestData(90, 90, 90);
        Assertions.assertDoesNotThrow(() -> Orchestrator.verifyWorkloadLimit(t3.config(), t3.instances(), t3.algorithms()));
    }

    private TestData getTestData(int repetitions, int nInstances, int nAlgorithms){
        List<Algorithm<TestSolution, TestInstance>> algorithms = new ArrayList<>();
        for (int i = 0; i < nAlgorithms; i++) {
            algorithms.add(new SimpleAlgorithm<TestSolution, TestInstance>(NoOp.constructive(), NoOp.improver()));
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
}
