package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.RandomizableNeighborhood;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;

/**
 * Executes random movements from the given neighborhood until there are no moves left to execute
 *
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class RandomConstructive<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Constructive<S, I> {

    private RandomizableNeighborhood<M,S,I> neighborhood;

    /**
     * Create a random constructive that will build solution by choosing random movements from the given neighborhood
     *
     * @param neighborhood neighborhood to use during construction
     */
    public RandomConstructive(RandomizableNeighborhood<M,S,I> neighborhood) {
        this.neighborhood = neighborhood;
    }

    /** {@inheritDoc} */
    @Override
    public S construct(S solution) {
        return exhaustNeighborhood(solution, neighborhood);
    }

    private S exhaustNeighborhood(S s, RandomizableNeighborhood<M,S,I> neighborhood) {
        Optional<? extends Move<S,I>> move;
        while((move = neighborhood.getRandomMove(s)).isPresent()){
            move.get().execute();
        }

        return s;
    }
}
