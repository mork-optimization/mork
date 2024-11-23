package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestMove extends Move<TestSolution, TestInstance> {
    private final double delta;
    private final FMode fmode;

    public static List<TestMove> generateSeq(double... data){
        return generateSeq("TestInstance", data);
    }

    public static List<TestMove> generateSeq(String instanceName, double... data){
        var testInstance = new TestInstance(instanceName);
        var testSolution = new TestSolution(testInstance);
        var moves = new ArrayList<TestMove>();
        for(var i: data){
            moves.add(new TestMove(testSolution, i));
        }
        return moves;
    }

    public TestMove(TestSolution solution, double delta, FMode fmode) {
        super(solution);
        this.delta = delta;
        this.fmode = fmode;
    }

    public TestMove(TestSolution solution, double v) {
        this(solution, v, FMode.MINIMIZE);
    }

    @Override
    protected TestSolution _execute(TestSolution solution) {
        solution.setScore(solution.getScore() + this.delta);
        return solution;
    }

    public double getScoreChange() {
        return delta;
    }

    @Override
    public String toString() {
        return "TestMove{" +
                "score=" + delta +
                ", maximizing=" + fmode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestMove testMove = (TestMove) o;
        return Double.compare(testMove.delta, delta) == 0 && fmode == testMove.fmode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(delta, fmode);
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
