package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.logging.Logger;

/**
 * All neighborhood moves should be represented by implementations of either EagerMove or LazyMove.
 * As both are in the same package as the Solution, they may directly manipulate it.
 * See the following references for differences between an EagerMove and a LazyMove
 *
 * @see es.urjc.etsii.grafo.solution.neighborhood.Neighborhood
 */
public abstract class LazyMove<M extends LazyMove<M,S,I>, S extends Solution<S,I>, I extends Instance> extends Move<S,I>{

    private static final Logger logger = Logger.getLogger(LazyMove.class.getName());

    /**
     * <p>Constructor for LazyMove.</p>
     *
     * @param solution a S object.
     */
    public LazyMove(S solution) {
        super(solution);
    }

    /**
     * Get next move in this sequence.
     *
     * @param solution solution used to generate the previous move,
     *                and where data will be picked for the current move
     * @return the next move in this generator sequence if there is a next move, null otherwise
     */
    public abstract M next(S solution);

}
