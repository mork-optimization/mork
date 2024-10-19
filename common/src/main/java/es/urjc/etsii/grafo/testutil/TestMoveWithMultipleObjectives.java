package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TestMoveWithMultipleObjectives extends Move<TestSolutionWithMultipleObjectives, TestInstance> {
    private final double[] score;
    private final FMode[] fmode;

    public static List<TestMoveWithMultipleObjectives> generateSeq(double[]... data){
        return generateSeq("TestInstance", data);
    }

    public static List<TestMoveWithMultipleObjectives> generateSeq(String instanceName, double[]... data){
        var testInstance = new TestInstance(instanceName);
        var testSolution = new TestSolutionWithMultipleObjectives(testInstance);
        var moves = new ArrayList<TestMoveWithMultipleObjectives>();
        for(var i: data){
            moves.add(new TestMoveWithMultipleObjectives(testSolution, i));
        }
        return moves;
    }

    public TestMoveWithMultipleObjectives(TestSolutionWithMultipleObjectives solution, double[] score, FMode[] fmode) {
        super(solution);
        this.score = score;
        this.fmode = fmode;
    }

    public TestMoveWithMultipleObjectives(TestSolutionWithMultipleObjectives solution, double[] v) {
        super(solution);
        this.score = v;
        this.fmode = new FMode[v.length];
        for (int i = 0; i < v.length; i++) {
            fmode[i] = FMode.MINIMIZE;
        }
    }

    @Override
    protected TestSolutionWithMultipleObjectives _execute(TestSolutionWithMultipleObjectives solution) {
        int i = 0;
        for (double v : score) {
            solution.scores[i] += this.score[i];
            i++;
        }
        return solution;
    }

    public double[] getScoreChanges() {
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
        TestMoveWithMultipleObjectives testMove = (TestMoveWithMultipleObjectives) o;
        if (this.score.length != testMove.score.length) {
            return false;
        }
        for (int i = 0; i < this.score.length; i++) {
            if (Double.compare(testMove.score[i], score[i]) != 0 && fmode[i] != testMove.fmode[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(score), Arrays.hashCode(fmode));
    }

    public boolean isMaximizing(int i) {
        return fmode.length > i && fmode[i] == FMode.MAXIMIZE;
    }

    public boolean isMinimizing(int i) {
        return fmode.length > i && fmode[i] == FMode.MINIMIZE;
    }

    public FMode[] getFmode() {
        return fmode;
    }
}
