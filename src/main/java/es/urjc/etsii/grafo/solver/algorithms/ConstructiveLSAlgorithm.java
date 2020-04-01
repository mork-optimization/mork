package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConstructiveLSAlgorithm<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S,I>{

    List<Improver<S,I>> improvers;
    Constructive<S,I> constructive;

    @SafeVarargs
    ConstructiveLSAlgorithm(SolutionBuilder<S,I> builder, Constructive<S, I> constructive, Improver<S,I>... improvers){
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
        for (var ls : improvers) {
            //TODO Store how much each local search contributes as metadata in the result, result subclass or somewhere.
            solution = ls.improve(solution);
        }
        return solution;
    }

}
