package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TestSolution extends Solution<TestSolution, TestInstance> {

    public static TestSolution[] from(double... scores) {
        return from(new TestInstance("TestInstance"), scores);
    }

    public static TestSolution[] from(TestInstance instance, double... scores) {
        var solutions = new TestSolution[scores.length];
        for (int i = 0; i < scores.length; i++) {
            solutions[i] = new TestSolution(instance, scores[i]);
        }
        return solutions;
    }

    protected double[] score = new double[1];

    Map<String, Function<TestSolution, Object>> properties = new HashMap<>();


    public TestSolution(TestInstance ins) {
        super(ins);
    }

    public TestSolution(TestInstance ins, double score) {
        this(ins);
        this.score = new double[]{score};
    }

    public TestSolution(TestInstance ins, double[] score) {
        this(ins);
        this.score = score;
    }

    public TestSolution(TestInstance ins, double score, Map<String, Function<TestSolution, Object>> properties) {
        this(ins);
        this.score = new double[]{score};
        this.properties = properties;
    }

    public TestSolution(TestInstance ins, double[] score, Map<String, Function<TestSolution, Object>> properties) {
        this(ins);
        this.score = score;
        this.properties = properties;
    }

    public TestSolution(TestSolution sol) {
        super(sol);
        this.score = sol.score.clone();
    }


    @Override
    public TestSolution cloneSolution() {
        return new TestSolution(this);
    }

    public double getScore() {
        return getScore(0);
    }

    public double getScore(int i) {
        return this.score[i];
    }

    public void setScore(double score) {
        setScore(0, score);
    }

    public void setScore(int i, double score) {
        this.score[i] = score;
    }

    @Override
    public String toString() {
        return "TestSolution";
    }

    public void setTTB(long ttb) {
        this.lastModifiedTime = ttb;
    }

    public void resetTTB() {
        this.lastModifiedTime = Integer.MIN_VALUE;
    }


    @Override
    public Map<String, Function<TestSolution, Object>> customProperties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestSolution that = (TestSolution) o;
        return Objects.deepEquals(score, that.score) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(score), properties);
    }
}
