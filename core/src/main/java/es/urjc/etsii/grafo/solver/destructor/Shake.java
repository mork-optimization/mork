package es.urjc.etsii.grafo.solver.destructor;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Different ways to shake a solution, RandomShake for a reference implementation
 *
 * @param <S> Solution class
 * @param <I> Instance class
 * @see RandomMoveShake
 */
public abstract class Shake<S extends Solution<S,I>, I extends Instance> {

    /**
     * Shake the solution. Use k to calculate how powerful the shake should be in your implementation.
     * Can be as simple as number of elements to remove, or to swap. Whatever you want.
     *
     * @param s Solution to shake
     * @param k shake strength
     * @return shaken solution. Shaken, not stirred.
     */
    public abstract S shake(S s, int k);
}
