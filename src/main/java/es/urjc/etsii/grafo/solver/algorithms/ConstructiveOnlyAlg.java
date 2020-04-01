package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;

public class ConstructiveOnlyAlg<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S,I> {

    private final Constructive<S, I> constructive;

    public ConstructiveOnlyAlg(Constructive<S, I> constructive, SolutionBuilder<S,I> builder) {
        super(builder);
        this.constructive = constructive;
    }

    @Override
    protected Solution<I> algorithm(I ins) {
        return this.constructive.construct(ins, builder);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "constructive=" + constructive +
                '}';
    }
}
