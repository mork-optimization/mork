package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

public class TestSolution extends Solution<TestSolution, TestInstance> {

    protected double score;

    public TestSolution(TestInstance ins) {
        super(ins);
    }

    public TestSolution(TestInstance ins, double score) {
        this(ins);
        this.score = score;
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
        return this.score;
    }

    public void setScore(double score){
        this.score = score;
    }

    @Override
    public double recalculateScore() {
        return this.score;
    }

    @Override
    public String toString() {
        return "TestSolution";
    }
}
