package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Move;

import java.util.Objects;

public class TestMove extends Move<TestSolution, TestInstance> {
    private final double score;
    private final FMode fmode;

    public TestMove(TestSolution solution, double score, FMode fmode) {
        super(solution);
        this.score = score;
        this.fmode = fmode;
    }

    public TestMove(TestSolution solution, double v) {
        this(solution, v, FMode.MINIMIZE);
    }

    @Override
    protected TestSolution _execute(TestSolution solution) {
        solution.score += this.score;
        return solution;
    }

    @Override
    public double getValue() {
        return score;
    }

    @Override
    public String toString() {
        return "TestMove{" +
                "score=" + score +
                ", maximizing=" + fmode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestMove testMove = (TestMove) o;
        return Double.compare(testMove.score, score) == 0 && fmode == testMove.fmode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, fmode);
    }

    public boolean isMaximizing() {
        return fmode == FMode.MAXIMIZE;
    }

    public boolean isMinimizing() {
        return fmode == FMode.MINIMIZE;
    }

    public FMode getFmode() {
        return fmode;
    }
}
