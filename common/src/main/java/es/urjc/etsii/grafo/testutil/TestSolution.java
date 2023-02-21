package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.*;
import java.util.function.Function;

public class TestSolution extends Solution<TestSolution, TestInstance> {

    public static TestSolution[] from(TestInstance instance, double... scores) {
        var solutions = new TestSolution[scores.length];
        for (int i = 0; i < scores.length; i++) {
            solutions[i] = new TestSolution(instance, scores[i]);
        }
        return solutions;
    }

    protected double score;

    Map<String, Function<TestSolution, Object>> properties = new HashMap<>();


    public TestSolution(TestInstance ins) {
        super(ins);
    }

    public TestSolution(TestInstance ins, double score) {
        this(ins);
        this.score = score;
    }

    public TestSolution(TestInstance ins, double score, Map<String, Function<TestSolution, Object>> properties) {
        this(ins);
        this.score = score;
        this.properties = properties;
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

    public void setTTB(long ttb){
        this.lastModifiedTime = ttb;
    }

    public void resetTTB(){
        this.lastModifiedTime = Integer.MIN_VALUE;
    }


    @Override
    public Map<String, Function<TestSolution, Object>> customProperties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSolution that = (TestSolution) o;
        return Double.compare(that.score, score) == 0 && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, properties);
    }
}
