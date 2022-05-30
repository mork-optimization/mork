package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;

public class TestAlgorithm extends Algorithm<TestSolution, TestInstance> {

    private final String algName;

    public TestAlgorithm(String algName) {
        this.algName = algName;
        this.setBuilder(new SolutionBuilder<>() {
            @Override
            public TestSolution initializeSolution(TestInstance testInstance) {
                return new TestSolution(testInstance);
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

    @Override
    public String getShortName() {
        return this.algName;
    }
}
