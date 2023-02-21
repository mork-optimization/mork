package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class TestSolutionBuilder extends SolutionBuilder<TestSolution, TestInstance> {
    @Override
    public TestSolution initializeSolution(TestInstance instance) {
        return new TestSolution(instance);
    }
}
