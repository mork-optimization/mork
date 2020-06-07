package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Optional;

/**
 * Neighborhood that is able to generate random movements under demand
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class RandomizableNeighborhood<S extends Solution<I>, I extends Instance> extends Neighborhood<S,I>{

    /**
     * Pick a random move within the neighborhood
     * @return a random move, if there is at least one valid move
     */
    public abstract Optional<? extends Move<S,I>> getRandomMove(S s);

}
