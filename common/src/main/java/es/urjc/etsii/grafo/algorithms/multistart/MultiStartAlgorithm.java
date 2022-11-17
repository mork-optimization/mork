package es.urjc.etsii.grafo.algorithms.multistart;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example multistart algorithm, executes a user-defined algorithm until N iterations are reached, return best found.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class MultiStartAlgorithm<S extends Solution<S,I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = LoggerFactory.getLogger(MultiStartAlgorithm.class);

    /**
     * Algorithm
     */
    protected final Algorithm<S, I> algorithm;

    /**
     * Maximum number of iterations
     */
    protected final int maxIterations;

    /**
     * Minimum number of iterations
     */
    protected final int minIterations;

    /**
     * Maximum number of iteration without improving. When the algorithm has been executed this number of time
     * without finding a new best solution, the multistart procedure ends.
     */
    protected final int maxIterationsWithoutImproving;

    /**
     * Use the {@link MultiStartAlgorithmBuilder} class to generate a MultiStart Algorithm
     *
     * @param algorithmName                 algorithm name
     * @param algorithm                     algorithm
     * @param maxIterations                 maximum number of iterations
     * @param minIterations                 minimum number of iteration the algorithm will be run. Must be less or equatl than the maximum number of iterations.
     * @param maxIterationsWithoutImproving number of iterations the algorithm should be run without improving before stop
     */
    protected MultiStartAlgorithm(String algorithmName, Algorithm<S, I> algorithm, int maxIterations, int minIterations, int maxIterationsWithoutImproving) {
        super(algorithmName);
        checkParameters(maxIterations, minIterations, maxIterationsWithoutImproving);
        this.algorithm = algorithm;
        this.maxIterations = maxIterations;
        this.minIterations = minIterations;
        this.maxIterationsWithoutImproving = maxIterationsWithoutImproving;
    }

    /**
     * Method to check if the termination criteria of the multistart procedure are correct.
     *
     * @param maxIterations                 maximum number of iterations
     * @param minIterations                 minimum number of iteration the algorithm will be run. Must be less or equatl than the maximum number of iterations.
     * @param maxIterationsWithoutImproving number of iterations the algorithm should be run without improving before stop
     */
    private void checkParameters(int maxIterations, int minIterations, int maxIterationsWithoutImproving) {
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
        while (!terminationCriteriaIsMet(iter, iterWI)) {
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
     * @return true if the termination criteria is met, false otherwise
     */
    private boolean terminationCriteriaIsMet(int iter, int iterWI) {
        if(TimeControl.isTimeUp()){
            return true;
        }
        if (iter >= this.maxIterations) {
            return true;
        }
        return iter >= this.minIterations && iterWI >= this.maxIterationsWithoutImproving;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "MA{" +
                "name=" + getShortName() +
                ", mxIter=" + maxIterations +
                ", mnIter=" + minIterations +
                ", mxIterWI=" + maxIterationsWithoutImproving +
                "}";
    }

    /**
     * Print the current status of the VNS procedure, i.e., the current iteration the best solution.
     *
     * @param iteration current iteration of the procedure
     * @param solution solution
     */
    protected void printStatus(int iteration, S solution) {
        log.debug("\t\t{}: {}", iteration, solution);
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
