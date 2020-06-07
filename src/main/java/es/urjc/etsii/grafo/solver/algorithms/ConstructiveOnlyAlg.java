package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;

public class ConstructiveOnlyAlg<S extends Solution<I>, I extends Instance> implements Algorithm<S,I> {

    private final Constructive<S, I> constructive;

    public ConstructiveOnlyAlg(Constructive<S, I> constructive, SolutionBuilder<S,I> builder) {
        this.constructive = constructive;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "constructive=" + constructive +
                '}';
    }

    @Override
    public S algorithm(I instance, SolutionBuilder<S, I> builder) {
        var solution = builder.initializeSolution(instance);
        return this.constructive.construct(solution);
    }
}
