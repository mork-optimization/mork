package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.stream.Stream;

/**
 * Defines a neighbourhood.
 * A neighborhoods represents all potential solutions that can be reached for a given solution applying a given move.
 * Usually used inside, but not limited to, a local search procedure,
 */
public abstract class Neighborhood<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     *
     * @param s Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<M> stream(S s);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
