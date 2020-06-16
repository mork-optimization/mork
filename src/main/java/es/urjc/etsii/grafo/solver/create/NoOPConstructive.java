package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Does nothing, return the solution as is
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class NoOPConstructive<S extends Solution<I>, I extends Instance> extends Constructive<S,I>{

    /**
     * Return the solution exactly as is
     * @param s Solution to initialize
     * @return same solution, no changes applied
     */
    @Override
    public S construct(S s) {
        return s;
    }
}
