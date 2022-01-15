package es.urjc.etsii.grafo.solver.destructor;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Method that destroys a solution, in part or completely.
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Destructive<S extends Solution<S,I>, I extends Instance> {

    /**
     * Destroy a part of the solution. Usually involves deassigning parts of the solution
     * to be rebuilt later by a reconstructive method
     * @param s original solution
     * @param k destroy intensity, may be ignored by the implementing class
     * @return reference to the modified solution. Can be modified in placed or cloned before.
     * // TODO clone always or not? decided by the caller?
     */
    public abstract S destroy(S s, int k);
}
