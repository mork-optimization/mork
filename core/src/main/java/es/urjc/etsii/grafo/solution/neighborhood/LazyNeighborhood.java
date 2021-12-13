package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Defines a neighbourhood.
 * A neighborhoods represents all potential solutions that can be reached for a given solution applying a given move.
 * Usually used inside, but not limited to, a local search procedure.
 * Movements in this neighborhood are generated lazily under demand using Streams with LazyMoves.
 */
public abstract class LazyNeighborhood<M extends LazyMove<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I>{

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     *
     * @param s Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<M> stream(S s);

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood given an initial move
     *
     * @param move initial move
     * @return Stream with all the available moves in the neighborhood
     */
    protected Stream<M> buildStream(M move){
        return Stream.iterate(move, Objects::nonNull, m -> (M) m.next());
    }
}
