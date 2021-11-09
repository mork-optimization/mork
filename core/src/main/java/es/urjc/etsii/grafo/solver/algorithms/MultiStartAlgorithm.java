package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Example multistart algorithm, executes a user-defined algorithm until N iterations are reached, return best found.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class MultiStartAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = Logger.getLogger(MultiStartAlgorithm.class.getName());

    final String algorithmName;
    final Algorithm<S, I> algorithm;
    private final int maxIterations;
    private final int minIterations;
    private final int maxIterationsWithoutImproving;
    private final long nanoTime;


    /**
     * Use the {@link MultiStartAlgorithmBuilder} class to generate a MultiStart Algorithm
     *
     * @param algorithmName                 algorithm name
     * @param algorithm                     algorithm
     * @param maxIterations                 maximum number of iterations
     * @param minIterations                 minimum number of iteration the algorithm will be run. Must be less or equatl than the maximum number of iterations.
     * @param maxIterationsWithoutImproving number of iterations the algorithm should be run without improving before stop
     * @param maxTime                       maximum allowed time
     * @param timeUnit                      Time unit of the maximum allowed time (i.e., SECONDS, DAYS, etc.)
     */
    private MultiStartAlgorithm(String algorithmName, Algorithm<S, I> algorithm, int maxIterations, int minIterations, int maxIterationsWithoutImproving, int maxTime, TimeUnit timeUnit) {
        checkParameters(maxIterations, minIterations, maxIterationsWithoutImproving, maxTime);
        this.algorithmName = algorithmName;
        this.algorithm = algorithm;
        this.maxIterations = maxIterations;
        this.minIterations = minIterations;
        this.maxIterationsWithoutImproving = maxIterationsWithoutImproving;
        this.nanoTime = timeUnit.toNanos(maxTime);
    }


    public static <S extends Solution<I>, I extends Instance> MultiStartAlgorithmBuilder<S, I> builder() {
        return new MultiStartAlgorithmBuilder<>();
    }

    /**
     * Method to check if the termination criteria of the multistart procedure are correct.
     *
     * @param maxIterations                 maximum number of iterations
     * @param minIterations                 minimum number of iteration the algorithm will be run. Must be less or equatl than the maximum number of iterations.
     * @param maxIterationsWithoutImproving number of iterations the algorithm should be run without improving before stop
     * @param maxTime                       maximum allowed time
     */
    private void checkParameters(int maxIterations, int minIterations, int maxIterationsWithoutImproving, int maxTime) {
        if (minIterations <= 0) {
            throw new IllegalArgumentException("The minimum number should be greater than 0");
        }
        if (maxIterationsWithoutImproving <= 0) {
            throw new IllegalArgumentException("The number of iterations without improving should be greater than 0");
        }
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("The number of iterations should be greater than 0");
        }
        if (minIterations > maxIterations) {
            throw new IllegalArgumentException("The minimum number of iterations should be lower than the maximum number of iterations");
        }
        if (maxTime <= 0) {
            throw new IllegalArgumentException("The maximum running time of the experiment should be greater than 0");
        }

    }


    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     *
     * @param instance Empty solution
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(I instance) {
        S best = null;
        int iter = 0;
        int iterWI = 0;
        long startT = System.nanoTime();
        while (!terminationCriteriaIsMet(iter, iterWI, startT)) {
            iter++;
            iterWI++;
            S solution = this.algorithm.algorithm(instance);
            S oldBest = best;
            if (best == null) {
                best = solution;
                iterWI = 0;
            } else {
                best = best.getBetterSolution(solution);
                if (oldBest != best) {
                    iterWI = 0;
                }
            }

            printStatus(iter, best);
        }

        return best;
    }

    /**
     * Method to check it the termination criteria of the multistart algorithm is met
     *
     * @param iter   current number of iteration of the algorithm
     * @param iterWI currr
     * @param startT starting time
     * @return true if the termination criteria is met, false otherwise
     */
    private boolean terminationCriteriaIsMet(int iter, int iterWI, long startT) {
        if (iter >= this.maxIterations) {
            return true;
        }
        if (iter >= this.minIterations) {
            if (iterWI >= this.maxIterationsWithoutImproving) {
                return true;
            }
            return System.nanoTime() - startT > this.nanoTime;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MA{" +
                "alg=" + (algorithmName.equals("") ? algorithm : algorithmName) +
                ", mxIter=" + maxIterations +
                ", mnIter=" + minIterations +
                ", mxIterWI=" + maxIterationsWithoutImproving +
                ", mxT=" + nanoTime;
    }

    protected void printStatus(int iteration, S s) {
        log.fine(() -> String.format("\t\t%s: %s", iteration, s));
    }

    public static final class MultiStartAlgorithmBuilder<S extends Solution<I>, I extends Instance> {

        private String name = "";
        private int maxIterations = Integer.MAX_VALUE / 2;
        private int minIterations = 1;
        private int maxIterationsWithoutImproving = Integer.MAX_VALUE / 2;
        private int units = 365;
        private TimeUnit timeUnit = TimeUnit.DAYS;


        private MultiStartAlgorithmBuilder() {
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
            return new MultiStartAlgorithm<S, I>(name, algorithm, maxIterations, minIterations, maxIterationsWithoutImproving, units, timeUnit);
        }
    }
}
