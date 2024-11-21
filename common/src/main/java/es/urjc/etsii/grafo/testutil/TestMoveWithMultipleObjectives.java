package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Move;

import java.util.Arrays;
import java.util.Objects;

public class TestMoveWithMultipleObjectives extends Move<TestSolutionWithMultipleObjectives, TestInstance> {
    private final double[] deltas;
    private final FMode[] fmodes;

    public TestMoveWithMultipleObjectives(TestSolutionWithMultipleObjectives solution, double[] deltas, FMode[] fmodes) {
        super(solution);
        this.deltas = deltas.clone();
        this.fmodes = fmodes.clone();
    }

    public TestMoveWithMultipleObjectives(TestSolutionWithMultipleObjectives solution, double[] deltas) {
        super(solution);
        this.deltas = deltas.clone();
        this.fmodes = new FMode[deltas.length];
        for (int i = 0; i < deltas.length; i++) {
            fmodes[i] = FMode.MINIMIZE;
        }
    }

    @Override
    protected TestSolutionWithMultipleObjectives _execute(TestSolutionWithMultipleObjectives solution) {
        for (int j = 0; j < deltas.length; j++) {
            solution.scores[j] += this.deltas[j];
        }
        return solution;
    }

    public double[] getScoreChanges() {
        return deltas;
    }

    @Override
    public String toString() {
        return "TestMove{" +
                "score=" + Arrays.toString(deltas) +
                ", maximizing=" + Arrays.toString(fmodes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestMoveWithMultipleObjectives that = (TestMoveWithMultipleObjectives) o;
        return Objects.deepEquals(deltas, that.deltas) && Objects.deepEquals(fmodes, that.fmodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(deltas), Arrays.hashCode(fmodes));
    }

    public boolean isMaximizing(int i) {
        return fmodes.length > i && fmodes[i] == FMode.MAXIMIZE;
    }

    public boolean isMinimizing(int i) {
        return fmodes.length > i && fmodes[i] == FMode.MINIMIZE;
    }

    public FMode[] getFmodes() {
        return fmodes;
    }
}
