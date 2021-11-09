package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

public class TestSolution extends Solution<TestSolution, TestInstance> {

    protected double score;

    public TestSolution(TestInstance ins) {
        super(ins);
    }

    public TestSolution(TestSolution sol) {
        super(sol);
        this.score = sol.score;
    }


    @Override
    public TestSolution cloneSolution() {
        return new TestSolution(this);
    }

    @Override
    protected boolean _isBetterThan(TestSolution other) {
        return false;
    }


    @Override
    public double getScore() {
        return 0;
    }

    @Override
    public double recalculateScore() {
        return 0;
    }

    @Override
    public String toString() {
        return "TestSolution";
    }
}
