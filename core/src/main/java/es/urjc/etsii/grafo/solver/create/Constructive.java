package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Builds a new solution for the current problem. There are multiple strategies to create the solution:
 * Using greedy strategies, GRASP, random, etc.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Constructive<S extends Solution<S,I>, I extends Instance> {

    /**
     * Build a solution. Start with an empty solution, end when the solution is valid.
     *
     * @param s Empty solution, the result of calling the constructor.
     * @return A valid solution that fulfills all the problem constraints.
     */
    public abstract S construct(S s);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
