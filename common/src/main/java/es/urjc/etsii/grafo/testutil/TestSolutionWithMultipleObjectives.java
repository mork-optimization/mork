package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TestSolutionWithMultipleObjectives extends Solution<TestSolutionWithMultipleObjectives, TestInstance>{

    protected double[] scores;

    public static TestSolutionWithMultipleObjectives[] from(double... scores) {
        return from(new TestInstance("TestInstance"), scores);
    }

    public static TestSolutionWithMultipleObjectives[] from(TestInstance instance, double... scores) {
        var solutions = new TestSolutionWithMultipleObjectives[scores.length];
        for (int i = 0; i < scores.length; i++) {
            solutions[i] = new TestSolutionWithMultipleObjectives(instance, scores);
        }
        return solutions;
    }

    Map<String, Function<TestSolutionWithMultipleObjectives, Object>> properties = new HashMap<>();


    public TestSolutionWithMultipleObjectives(TestInstance ins) {
        super(ins);
    }

    public TestSolutionWithMultipleObjectives(TestInstance ins, double[] scores) {
        this(ins);
        this.scores = scores;
    }

    public TestSolutionWithMultipleObjectives(TestInstance ins, double[] scores, Map<String, Function<TestSolutionWithMultipleObjectives, Object>> properties) {
        this(ins);
        this.scores = scores;
        this.properties = properties;
    }

    public TestSolutionWithMultipleObjectives(TestSolutionWithMultipleObjectives sol) {
        super(sol);
        this.scores = sol.scores;
    }

    @Override
    public TestSolutionWithMultipleObjectives cloneSolution() {
        return new TestSolutionWithMultipleObjectives(this);
    }

    @Override
    public String toString() {
        return "TestSolutionWithMultipleObjectives{" +
                "score=" + scores.toString() +
                '}';
    }

    public void setTTB(long ttb) {
        this.lastModifiedTime = ttb;
    }

    public void resetTTB() {
        this.lastModifiedTime = Integer.MIN_VALUE;
    }


    @Override
    public Map<String, Function<TestSolutionWithMultipleObjectives, Object>> customProperties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSolutionWithMultipleObjectives that = (TestSolutionWithMultipleObjectives) o;
        if (this.scores.length != that.scores.length) {
            return false;
        }
        for (int i = 0; i < this.scores.length; i++) {
            if (Double.compare(that.scores[i], scores[i]) != 0){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.scores), properties);
    }

    public void setScores(double[] scores){
        this.scores = scores;
    }

    public double getObjective(int index){
        return this.scores[index];
    }

    public double[] getScores(){
        return this.scores;
    }

}
