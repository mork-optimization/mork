package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class VNS<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S, I> {

    private static Logger log = Logger.getLogger(VNS.class.getName());

    List<Improver<S, I>> improvers;
    Constructive<S, I> constructive;
    private final Shake<S, I> shake;
    private final int[] ks;
    private final int maxK;

    /**
     * Execute VNS until finished
     * @param ks Integer array that will be used for the shake/perturbation
     * @param builder How to build a Solution from the given Instance
     * @param shake Perturbation method
     * @param constructive Constructive method
     * @param improvers List of improvers/local searches
     */
    @SafeVarargs
    public VNS(int[] ks, SolutionBuilder<S, I> builder, Shake<S, I> shake, Constructive<S, I> constructive, Improver<S, I>... improvers) {
        super(builder);
        if (ks == null || ks.length == 0) {
            throw new IllegalArgumentException("Invalid Ks array, must have at least one element");
        }
        this.ks = ks;
        // Ensure Ks are sorted, maxK is the last element
        Arrays.sort(ks);
        this.maxK = ks[ks.length - 1];
        this.shake = shake;
        this.constructive = constructive;
        this.improvers = Arrays.asList(improvers);    }

    protected Solution<I> algorithm(I ins) {
        S solution = constructive.construct(ins, builder);
        solution = localSearch(solution);

        int currentKIndex = 0;
        while (currentKIndex < ks.length && !solution.stop()) {
            printStatus(String.valueOf(currentKIndex), solution);
            S copy = solution.cloneSolution();
            shake.shake(copy, this.ks[currentKIndex], maxK);
            copy = localSearch(copy);
            S bestSolution = copy.getBetterSolution(solution);
            if (bestSolution == solution) {   // No improve
                currentKIndex++;
            } else {                        // Improved
                solution = copy;
                currentKIndex = 0;
            }
        }

        return solution;
    }

    private S localSearch(S solution) {
        for (Improver<S, I> ls : improvers) {
            solution = ls.improve(solution);
        }
        return solution;
    }

    private void printStatus(String phase, S s) {
        log.fine(String.format("\t\t\t%s: %s\n", phase, s));
    }

    @Override
    public String toString() {
        return "VNS{" +
                "improvers=" + improvers +
                ", constructive=" + constructive +
                ", shake=" + shake +
                ", ks=" + Arrays.toString(ks) +
                '}';
    }
}
