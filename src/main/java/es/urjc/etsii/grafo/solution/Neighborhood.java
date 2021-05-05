package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Defines a neighbourhood for the local search methods
 */
public abstract class Neighborhood<M extends Move<S,I>, S extends Solution<I>, I extends Instance> {

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<M> stream(S s);

    protected Stream<M> buildStream(M move){
        return Stream.iterate(move, Objects::nonNull, m -> (M) m.next());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
