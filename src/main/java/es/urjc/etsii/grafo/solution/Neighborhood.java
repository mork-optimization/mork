package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines a neighbourhood for the local search methods
 */
public abstract class Neighborhood<S extends Solution<I>, I extends Instance> {

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<? extends Move<S,I>> stream(S s);

    /**
     * Pick a random move within the neighborhood
     * @return a random move, if there is at least one valid move
     */
    public abstract Optional<? extends Move<S,I>> getRandomMove(S s);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
