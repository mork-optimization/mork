package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.logging.Logger;

/**
 * All neighborhood moves should be represented by implementations of either EagerMove or LazyMove.
 * As both are in the same package as the Solution, they may directly manipulate it.
 * See the following references for differences between an EagerMove and a LazyMove
 *
 * @see es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood
 * @see es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood
 */
public abstract class EagerMove<S extends Solution<S,I>, I extends Instance> extends Move<S,I>{

    private static final Logger logger = Logger.getLogger(EagerMove.class.getName());

    /**
     * Create a new eager move
     *
     * @param s Solution this move refers to
     */
    public EagerMove(S s) {
        super(s);
    }
}
