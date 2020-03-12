package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructor;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;

import java.util.function.Supplier;

public class ConstructionOnlyAlgorithm<S extends Solution<I>, I extends Instance> extends BaseAlgorithm<S,I> {

    SolutionBuilder<S,I> builder;

    public ConstructionOnlyAlgorithm(Supplier<Constructor<S, I>> constructorSupplier, SolutionBuilder<S,I> builder) {
        super(constructorSupplier);
        this.builder = builder;
    }

    @Override
    protected Solution<I> algorithm(I ins) {
        return this.constructor.construct(ins, builder);
    }
}
