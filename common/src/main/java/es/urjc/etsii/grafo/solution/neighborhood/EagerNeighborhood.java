package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.EagerMove;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.stream.Stream;

/**
 * Defines a neighbourhood.
 * A neighborhoods represents all potential solutions that can be reached for a given solution applying a given move.
 * Usually used inside, but not limited to, a local search procedure.
 * Movements in this neighborhood are generated at once, returning a Collection of EagerMove.
 */
public abstract class EagerNeighborhood<M extends EagerMove<S,I>, S extends Solution<S,I>, I extends Instance> extends Neighborhood<M,S,I>{

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     *
     * @param s Solution used to generate the neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public Stream<M> stream(S s){
        return getMovements(s).stream();
    }

    /**
     * Return a collection with all possible movements for the given solution in this neighborhood.
     *
     * @param s Solution
     * @return Collection of movements
     */
    public abstract List<M> getMovements(S s);
}
