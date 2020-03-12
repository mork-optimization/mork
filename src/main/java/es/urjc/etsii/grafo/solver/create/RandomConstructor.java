package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.ConstructiveNeighborhood;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.function.Supplier;

public class RandomConstructor<S extends Solution<I>, I extends Instance> extends Constructor<S, I>  {

    protected final Supplier<? extends RuntimeException> NOT_ENOUGH_MOVES = () -> new RuntimeException("Solution is not in a valid state but we do not have any available moves");

    private ConstructiveNeighborhood<S,I> neighborhood;

    public RandomConstructor(ConstructiveNeighborhood<S,I> neighborhood) {
        this.neighborhood = neighborhood;
    }

    @Override
    public S construct(I i, SolutionBuilder<S,I> builder) {
        return assignMissing(builder.initializeSolution(i), neighborhood);
    }

    private S assignMissing(S s, ConstructiveNeighborhood<S,I> neighborhood) {
        while(!s.isValid()){
            var move = neighborhood.getRandomMove(s).orElseThrow(NOT_ENOUGH_MOVES);
            move.execute();
        }
        return s;
    }
}
