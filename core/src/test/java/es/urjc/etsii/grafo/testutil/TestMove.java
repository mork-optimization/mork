package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Objects;

public class TestMove extends Move<TestSolution, TestInstance> {
    private final double score;
    private final boolean maximizing;

    public TestMove(TestSolution solution, double score, boolean maximizing) {
        super(solution);
        this.score = score;
        this.maximizing = maximizing;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected void _execute() {
        getSolution().score += this.score;
    }

    @Override
    public double getValue() {
        return score;
    }

    @Override
    public boolean improves() {
        double v = this.getValue();
        return maximizing ? DoubleComparator.isPositive(v) : DoubleComparator.isNegative(v);
    }

    @Override
    public String toString() {
        return "TestMove{" +
                "score=" + score +
                ", maximizing=" + maximizing +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestMove testMove = (TestMove) o;
        return Double.compare(testMove.score, score) == 0 && maximizing == testMove.maximizing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, maximizing);
    }
}
