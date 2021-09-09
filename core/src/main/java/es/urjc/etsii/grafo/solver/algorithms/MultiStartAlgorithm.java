package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.logging.Logger;

/**
 * Example multistart algorithm, executes a user-defined algorithm until N iterations are reached, return best found.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class MultiStartAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S, I> {

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    private final int n;
    final String algorithmName;
    final Algorithm<S, I> algorithm;


    public MultiStartAlgorithm(int n, Algorithm<S, I> algorithm) {
        this(n, "", algorithm);
    }

    public MultiStartAlgorithm(int n, String algorithmName, Algorithm<S, I> algorithm) {
        this.n = n;
        this.algorithmName = algorithmName;
        this.algorithm = algorithm;
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
        for (int i = 0; i < n; i++) {
            solution = this.algorithm.algorithm(solution);
            if (best == null) {
                best = solution;
            } else {
                best = best.getBetterSolution(solution);
            }
            printStatus(i, best);
        }

        return best;
    }

    @Override
    public String toString() {
        return "MA{" +
                "n=" + n +
                ", algorithm='" + (algorithmName.equals("") ? algorithm.toString() : algorithmName) +
                '}';
    }

    protected void printStatus(int iteration, S s) {
        log.fine(() -> String.format("\t\t%s: %s", iteration, s));
    }

}
