package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example simple algorithm, executes:
 * Constructive → (Optional, if present) Local Searches → (Optional, if present) Shake → If did not improve end
 *                                               ^_________________________________________|   else repeat
 * This class can be used to test all the pieces if they are working properly, or as a base for more complex algorithms
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class SimpleAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S,I>{

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    final Constructive<S,I> constructive;
    final List<Improver<S, I>> improvers;
    final String algorithmName;

    @SafeVarargs
    public SimpleAlgorithm(Constructive<S, I> constructive, Improver<S,I>... improvers){
        this("", constructive, improvers);
    }

    @SafeVarargs
    public SimpleAlgorithm(String algorithmName, Constructive<S, I> constructive, Improver<S,I>... improvers){
        this.algorithmName = algorithmName.trim();
        this.constructive = constructive;
        if(improvers != null && improvers.length >= 1){
            this.improvers = Arrays.asList(improvers);
        } else {
            this.improvers = new ArrayList<>();
        }
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     * @param instance Instance
     * @return Returns a valid solution
     */
    @Override
    public S algorithm(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        ValidationUtil.validSolution(solution);
        printStatus("Constructive", solution);
        solution = localSearch(solution);
        return solution;
    }

    protected S localSearch(S solution) {
        for (int i = 0; i < improvers.size(); i++) {
            Improver<S, I> ls = improvers.get(i);
            solution = ls.improve(solution);
            ValidationUtil.validSolution(solution);
            printStatus("Improver " + i, solution);
        }
        return solution;
    }

    protected void printStatus(String phase, S s){
        log.fine(() -> String.format("\t\t%s: %s", phase, s));
    }

    @Override
    public String toString() {
        return "S{" +
                "c=" + constructive +
                ", i=" + improvers +
                '}';
    }

    @Override
    public String getShortName() {
        return this.algorithmName.isEmpty() ? super.getShortName() : algorithmName;
    }
}
