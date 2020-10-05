package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example multistart algorithm, executes:
 * Constructive --> (Optional, if present) Local Searches
 *    ^_________________________________________|   repeat until N iterations, return best found.
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class SimpleMultiStartAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S,I>{

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    private final int n;
    Constructive<S,I> constructive;
    List<Improver<S,I>> improvers;

    @SafeVarargs
    public SimpleMultiStartAlgorithm(int n, Constructive<S, I> constructive, Improver<S,I>... improvers){
        this.n = n;
        this.constructive = constructive;
        if(improvers != null && improvers.length >= 1){
            this.improvers = Arrays.asList(improvers);
        }
    }

    public SimpleMultiStartAlgorithm(int n, Constructive<S, I> constructive){
        this(n, constructive, (Improver<S, I>[]) null);
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     * @param instance Instance the algorithm will use
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(I instance, SolutionBuilder<S,I> builder) {
        S best = null;
        for (int i = 0; i < n; i++) {
            S solution = builder.initializeSolution(instance);
            solution = constructive.construct(solution);
            printStatus("Constructive", solution);
            solution = localSearch(solution);
            if(best == null){
                best = solution;
            } else {
                best = best.getBetterSolution(solution);
            }
        }

        return best;
    }

    private S localSearch(S solution) {
        if(improvers != null){
            for (int i = 0; i < improvers.size(); i++) {
                Improver<S, I> ls = improvers.get(i);
                solution = ls.improve(solution);
                printStatus("Improver " + i, solution);
            }
        }
        return solution;
    }

    private void printStatus(String phase, S s){
        log.fine(() -> String.format("\t\t%s: %s", phase, s));
    }

    @Override
    public String toString() {
        return "SimpleMultiStart{" +
                "n=" + n +
                ", cnstr=" + constructive +
                ", impr=" + improvers +
                '}';
    }
}
