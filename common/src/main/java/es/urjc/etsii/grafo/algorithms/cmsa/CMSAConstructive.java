package es.urjc.etsii.grafo.algorithms.cmsa;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Set;

/**
 * Constructive method used by {@link CMSA} to probabilistically build complete solutions.
 * Solutions built by this method are only used to sample which solution components (edges, assignments,
 * items, etc.) look promising: they are never returned as the algorithm result directly.
 * <p>
 * Besides building a solution as any other {@link Constructive}, implementations must be able to identify
 * which solution components were used to build a given solution, so {@link CMSA} can add them to the
 * restricted sub-instance solved at every iteration, and track their age.
 * <p>
 * Solution components can be represented using any type that correctly implements {@code equals} and
 * {@code hashCode}, for example a record such as {@code record Edge(int from, int to)}, or an autoboxed
 * primitive such as {@code Integer} when components are simply indexes.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 * @param <C> Solution component class
 */
public abstract class CMSAConstructive<S extends Solution<S, I>, I extends Instance, C> extends Constructive<S, I> {

    /**
     * Returns the solution components used to build the given solution.
     * This method is usually called immediately after {@link #construct(Solution)},
     * but implementations should not assume this and must be able to work with any
     * feasible solution to the problem, as it is also used to identify which components
     * are part of the solution returned by the {@link CMSASolver}.
     *
     * @param solution a feasible solution to the problem
     * @return the set of solution components used in the given solution
     */
    public abstract Set<C> usedComponents(S solution);
}
