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

public class VNS<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S,I> {

    private static Logger log = Logger.getLogger(VNS.class.getName());

    List<Improver<S,I>> improvers;
    Constructive<S,I> constructive;
    private final Shake<S, I> shake;
    private final double[] ks;

    @SafeVarargs
    public VNS(double[] ks, SolutionBuilder<S,I> builder, Shake<S,I> shake, Constructive<S, I> constructive, Improver<S,I>... improvers){
        super(builder);
        this.ks = ks;
        this.shake = shake;
        this.constructive = constructive;
        this.improvers = Arrays.asList(improvers);
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     * @param ins Instance the algorithm will use
     * @return Returns a valid solution
     */
    protected Solution<I> algorithm(I ins){
        var solution = constructive.construct(ins, builder);
        solution = localSearch(solution);

        int currentKIndex = 0;
        while(currentKIndex < ks.length){
            printStatus(String.valueOf(currentKIndex), solution);
            S copy = solution.cloneSolution();
            shake.iteration(copy, this.ks[currentKIndex]);
            copy = localSearch(copy);
            if(copy.getBetterSolution(solution) == solution){
                currentKIndex++;
            } else {
                solution = copy;
                currentKIndex = 0;
            }
        }

        return solution;
    }

    private S localSearch(S solution){
        for (Improver<S, I> ls : improvers) {
            solution = ls.improve(solution);
        }
        return solution;
    }

    private void printStatus(String phase, S s){
        System.out.format("\t\t\t%s: %s\n", phase, s);
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
