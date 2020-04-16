package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.ConstructiveNeighborhood;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;

/**
 * Executes random movements from the given neighborhood until there are no moves left to execute
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class RandomConstructive<S extends Solution<I>, I extends Instance> extends Constructive<S, I> {

    private ConstructiveNeighborhood<S,I> neighborhood;

    public RandomConstructive(ConstructiveNeighborhood<S,I> neighborhood) {
        this.neighborhood = neighborhood;
    }

    @Override
    public S construct(I i, SolutionBuilder<S,I> builder) {
        return exhaustNeighborhood(builder.initializeSolution(i), neighborhood);
    }

    private S exhaustNeighborhood(S s, ConstructiveNeighborhood<S,I> neighborhood) {
        Optional<? extends Move<S,I>> move;
        while((move = neighborhood.getRandomMove(s)).isPresent()){
            move.get().execute();
        }

        return s;
    }
}
