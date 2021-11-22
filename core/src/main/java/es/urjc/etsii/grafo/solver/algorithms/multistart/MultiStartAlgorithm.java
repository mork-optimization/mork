package es.urjc.etsii.grafo.solver.algorithms.multistart;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Example multistart algorithm, executes a user-defined algorithm until N iterations are reached, return best found.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class MultiStartAlgorithm<S extends Solution<S,I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = Logger.getLogger(MultiStartAlgorithm.class.getName());

    /**
     * Algorithm name
     */
    final String algorithmName;
    /**
     * Algorithm
     */
    final Algorithm<S, I> algorithm;

    /**
     * Maximum number of iterations
     */
    private final int maxIterations;

    /**
     * Minimum number of iterations
     */
    private final int minIterations;

    /**
     * Maximum number of iteration without improving. When the algorithm has been executed this number of time
     * without finding a new best solution, the multistart procedure ends.
     */
    private final int maxIterationsWithoutImproving;

    /**
     * Cut off time in nanotime
     */
    private final long nanoTime;


    /**
     * Use the {@link es.urjc.etsii.grafo.solver.algorithms.multistart.MultiStartAlgorithmBuilder} class to generate a MultiStart Algorithm
     *
     * @param algorithmName                 algorithm name
     * @param algorithm                     algorithm
     * @param maxIterations                 maximum number of iterations
     * @param minIterations                 minimum number of iteration the algorithm will be run. Must be less or equatl than the maximum number of iterations.
     * @param maxIterationsWithoutImproving number of iterations the algorithm should be run without improving before stop
     * @param maxTime                       maximum allowed time
     * @param timeUnit                      Time unit of the maximum allowed time (i.e., SECONDS, DAYS, etc.)
     */
    protected MultiStartAlgorithm(String algorithmName, Algorithm<S, I> algorithm, int maxIterations, int minIterations, int maxIterationsWithoutImproving, int maxTime, TimeUnit timeUnit) {
        checkParameters(maxIterations, minIterations, maxIterationsWithoutImproving, maxTime);
        this.algorithmName = algorithmName;
        this.algorithm = algorithm;
        this.maxIterations = maxIterations;
        this.minIterations = minIterations;
        this.maxIterationsWithoutImproving = maxIterationsWithoutImproving;
        this.nanoTime = timeUnit.toNanos(maxTime);
    }


    /**
     * Build a multistart algorithm
     *
     * @param <S> Solution class
     * @param <I> Instance class
     * @return the multistart algorithm
     */
    public static <S extends Solution<S,I>, I extends Instance> MultiStartAlgorithmBuilder<S, I> builder() {
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
     * {@inheritDoc}
     *
     * Algorithm: Execute a single construction and then all the local searchs a single time.
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
            if(solution.isBetterThan(best)){
                best = solution;
                iterWI = 0;
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "MA{" +
                "alg=" + (algorithmName.equals("") ? algorithm : algorithmName) +
                ", mxIter=" + maxIterations +
                ", mnIter=" + minIterations +
                ", mxIterWI=" + maxIterationsWithoutImproving +
                ", mxT=" + nanoTime;
    }

    /**
     * Print the current status of the VNS procedure, i.e., the current iteration the best solution.
     *
     * @param iteration current iteration of the procedure
     * @param s solution
     */
    protected void printStatus(int iteration, S s) {
        log.fine(() -> String.format("\t\t%s: %s", iteration, s));
    }


    /**
     * {@inheritDoc}
     *
     * This method propagates the builder so that it can be used by other algorithms.
     */
    @Override
    public void setBuilder(SolutionBuilder<S, I> builder) {
        super.setBuilder(builder);
        this.algorithm.setBuilder(builder);
    }
}
