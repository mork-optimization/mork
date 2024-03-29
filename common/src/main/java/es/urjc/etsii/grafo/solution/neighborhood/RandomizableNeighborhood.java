package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;

/**
 * Neighborhood that is able to generate random movements under demand
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class RandomizableNeighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I> {

    /**
     * Pick a random move within the neighborhood
     *
     * @param solution Solution used  to generate the neighborhood
     * @return a random move, if there is at least one valid move
     */
    public abstract Optional<M> getRandomMove(S solution);

}
