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

    private static Logger log = Logger.getLogger(MultiStartAlgorithm.class.getName());


    final String algorithmName;
    final Algorithm<S, I> algorithm;
    private final int maxIterations;
    private final int minIterations;
    private final int maxIterationsWithoutImproving;
    private final long maxTime;

    public MultiStartAlgorithm(String algorithmName, Algorithm<S, I> algorithm, int maxIterations, int minIterations, int maxIterationsWithoutImproving, long maxTime) {
        this.algorithmName = algorithmName;
        this.algorithm = algorithm;
        this.maxIterations = maxIterations;
        this.minIterations = minIterations;
        this.maxIterationsWithoutImproving = maxIterationsWithoutImproving;
        this.maxTime = maxTime;
    }


    public MultiStartAlgorithm(String algorithmName, Algorithm<S, I> algorithm, int maxIterations) {
        this(algorithmName, algorithm, maxIterations, 0, Integer.MAX_VALUE, Long.MAX_VALUE);
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     *
     * @param solution Empty solution
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(S solution) {
        S best = null;
        int iter = 0;
        int iterWI = 0;
        long startT = System.nanoTime();
        while (terminationCriteriaIsMet(iter, iterWI, startT)) {
            iter++;
            iterWI++;
            solution = this.algorithm.algorithm(solution);
            if (best == null) {
                best = solution;
            } else {
                best = best.getBetterSolution(solution);
                iterWI = 0;
            }
            printStatus(iter, best);
        }

        return best;
    }

    private boolean terminationCriteriaIsMet(int iter, int iterWI, long startT) {
        if (iter > this.maxIterations) return true;
        if (iter > this.minIterations) {
            if (iterWI > this.maxIterationsWithoutImproving) return true;
            return TimeUnit.SECONDS.convert(System.nanoTime() - startT, TimeUnit.NANOSECONDS) > this.maxTime;
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
                ", mxT=" + maxTime;
    }

    protected void printStatus(int iteration, S s) {
        log.fine(() -> String.format("\t\t%s: %s", iteration, s));
    }

}
