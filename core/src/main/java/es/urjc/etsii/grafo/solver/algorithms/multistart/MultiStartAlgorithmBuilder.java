package es.urjc.etsii.grafo.solver.algorithms.multistart;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.util.concurrent.TimeUnit;

public class MultiStartAlgorithmBuilder<S extends Solution<S, I>, I extends Instance> {

    private String name = "";
    private int maxIterations = Integer.MAX_VALUE / 2;
    private int minIterations = 1;
    private int maxIterationsWithoutImproving = Integer.MAX_VALUE / 2;
    private int units = 365;
    private TimeUnit timeUnit = TimeUnit.DAYS;


    /**
     * Use MultiStartAlgorithmBuilder::builder static method instead
     */
    protected MultiStartAlgorithmBuilder() {
    }


    /**
     * @param name name of the algorithm
     * @return MultiStartAlgorithmBuilder
     */
    public MultiStartAlgorithmBuilder<S, I> withAlgorithmName(String name) {
        this.name = name;
        return this;
    }


    /**
     * @param maxIterations maximum number of iteration of the algorithm
     * @return MultiStartAlgorithmBuilder
     */
    public MultiStartAlgorithmBuilder<S, I> withMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    /**
     * @param minIterations minimum number of iterations of the algorithm
     * @return MultiStartAlgorithmBuilder
     */
    public MultiStartAlgorithmBuilder<S, I> withMinIterations(int minIterations) {
        this.minIterations = minIterations;
        return this;
    }

    /**
     * @param maxIterationsWithoutImproving maximum number of iterations without improving
     * @return MultiStartAlgorithmBuilder
     */
    public MultiStartAlgorithmBuilder<S, I> withMaxIterationsWithoutImproving(int maxIterationsWithoutImproving) {
        this.maxIterationsWithoutImproving = maxIterationsWithoutImproving;
        return this;
    }


    /**
     * @param time     number of a spcefic time unit measure
     * @param timeUnit time unit measure: SECOND, DAY, etc.
     * @return MultiStartAlgorithmBuilder
     */
    public MultiStartAlgorithmBuilder<S, I> withTime(int time, TimeUnit timeUnit) {
        this.units = time;
        this.timeUnit = timeUnit;
        return this;
    }

    /**
     * Method to build a Multistart Algorithm
     *
     * @param algorithm algorithm
     * @return the multistart algorithm
     */
    public MultiStartAlgorithm<S, I> build(Algorithm<S, I> algorithm) {
        return new MultiStartAlgorithm<>(name, algorithm, maxIterations, minIterations, maxIterationsWithoutImproving, units, timeUnit);
    }
}
