package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TestSolutionWithMultipleObjectives extends Solution<TestSolutionWithMultipleObjectives, TestInstance>{

    protected double[] scores;

    public TestSolutionWithMultipleObjectives(TestInstance ins) {
        super(ins);
    }

    public TestSolutionWithMultipleObjectives(TestInstance ins, double[] scores) {
        this(ins);
        this.scores = scores.clone();
    }

    public TestSolutionWithMultipleObjectives(TestSolutionWithMultipleObjectives sol) {
        super(sol);
        this.scores = sol.scores.clone();
    }

    @Override
    public TestSolutionWithMultipleObjectives cloneSolution() {
        return new TestSolutionWithMultipleObjectives(this);
    }

    @Override
    public String toString() {
        return "TestSolutionWithMultipleObjectives{" +
                "score=" + Arrays.toString(scores) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestSolutionWithMultipleObjectives that = (TestSolutionWithMultipleObjectives) o;
        return Objects.deepEquals(scores, that.scores);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(scores);
    }

    public void setScore(int index, double value){
        this.scores[index] = value;
    }

    public void setScores(double[] scores){
        this.scores = scores.clone();
    }

    public double getObjective(int index){
        return this.scores[index];
    }

    public double[] getScores(){
        return this.scores;
    }

}
