package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

/**
 *
 */
public abstract class ConstructiveNeighborhood
        <
            M extends Move<S,I>,
            S extends Solution<I>,
            I extends Instance
        >
        extends Neighborhood<M, S, I> {

}
