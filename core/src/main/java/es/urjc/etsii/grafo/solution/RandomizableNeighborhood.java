package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Optional;

/**
 * Neighborhood that is able to generate random movements under demand
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public interface RandomizableNeighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {

    /**
     * Pick a random move within the neighborhood
     *
     * @param s Solution used  to generate the neighborhood
     * @return a random move, if there is at least one valid move
     */
    Optional<M> getRandomMove(S s);

}
