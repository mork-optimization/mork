package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.logging.Logger;

/**
 * Example multistart algorithm, executes:
 * Constructive → (Optional, if present) Local Searches
 *    ^_________________________________________|   repeat until N iterations, return best found.
 * @param <S> Solution class
 * @param <I> Instance class
 */
@Deprecated(forRemoval = true)
public class SimpleMultiStartAlgorithm<S extends Solution<I>, I extends Instance> extends SimpleAlgorithm<S,I>{

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    private final int n;

    @SafeVarargs
    public SimpleMultiStartAlgorithm(int n, Constructive<S, I> constructive, Improver<S,I>... improvers){
        super(constructive, improvers);
        this.n = n;
    }

    @SafeVarargs
    public SimpleMultiStartAlgorithm(int n, String algorithmName, Constructive<S, I> constructive, Improver<S,I>... improvers){
        super(algorithmName, constructive, improvers);
        this.n = n;
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs N times, returning best solution of all iterations.
     * @param instance Instance
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(I instance) {
        S best = null;
        for (int i = 0; i < n; i++) {
            var solution = super.algorithm(instance);
            if(best == null){
                best = solution;
            } else {
                best = best.getBetterSolution(solution);
            }
        }

        return best;
    }

    @Override
    public String toString() {
        return "MS{" +
                "n=" + n +
                ", c=" + constructive +
                ", i=" + improvers +
                '}';
    }
}
