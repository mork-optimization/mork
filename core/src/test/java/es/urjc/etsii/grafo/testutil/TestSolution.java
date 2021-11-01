package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

public class TestSolution extends Solution<TestInstance> {

    public TestSolution(TestInstance ins) {
        super(ins);
    }

    public TestSolution(Solution<TestInstance> s) {
        super(s);
    }

    @Override
    public TestSolution cloneSolution() {
        return new TestSolution(this);
    }

    @Override
    public <S extends Solution<TestInstance>> S getBetterSolution(S o) {
        return null;
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
