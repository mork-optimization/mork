package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;

public class TestAlgorithm extends Algorithm<TestSolution, TestInstance> {


    public TestAlgorithm(String algName) {
        super(algName);
        this.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance instance) {
                return new TestSolution(instance);
            }
        });
    }

    public TestAlgorithm(){
        this("testAlgorithm");
    }

    @Override
    public TestSolution algorithm(TestInstance instance) {
        var solution = this.newSolution(instance);
        return solution;
    }

}
