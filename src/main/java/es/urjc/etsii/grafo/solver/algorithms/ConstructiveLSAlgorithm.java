package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ConstructiveLSAlgorithm<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S,I>{

    private static Logger log = Logger.getLogger(ConstructiveLSAlgorithm.class.getName());

    List<Improver<S,I>> improvers;
    Constructive<S,I> constructive;

    @SafeVarargs
    public ConstructiveLSAlgorithm(SolutionBuilder<S,I> builder, Constructive<S, I> constructive, Improver<S,I>... improvers){
        super(builder);
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
        printStatus("Constructive", solution);
        for (int i = 0; i < improvers.size(); i++) {
            Improver<S, I> ls = improvers.get(i);
            solution = ls.improve(solution);
            printStatus("Improver " + i, solution);
        }
        return solution;
    }

    private void printStatus(String phase, S s){
        //log.fine(() -> String.format("\t\t%s: %s", phase, s));
    }

    @Override
    public String toString() {
        return "ConstructiveLSAlgorithm{" +
                "constructive=" + constructive +
                ", improvers=" + improvers +
                '}';
    }
}
