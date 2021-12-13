package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Does nothing, return the solution as is
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class NoOPConstructive<S extends Solution<S,I>, I extends Instance> extends Constructive<S,I>{

    /**
     * {@inheritDoc}
     *
     * Return the solution exactly as is
     */
    @Override
    public S construct(S s) {
        return s;
    }
}
