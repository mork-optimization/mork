package es.urjc.etsii.grafo.create;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Builds a new solution for the current problem. There are multiple strategies to create the solution:
 * Using greedy strategies, GRASP, random, etc.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class Constructive<S extends Solution<S,I>, I extends Instance> {

    /**
     * Build a solution. Start with an empty solution, end when the solution is valid.
     *
     * @param solution Empty solution, the result of calling the constructor.
     * @return A valid solution that fulfills all the problem constraints.
     */
    public abstract S construct(S solution);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }

    /**
     * Create a no operation constructive method
     * Returns the solution immediately without executing any operation
     * @param <S> Solution class
     * @param <I> Instance class
     * @return Null improve method
     */
    public static <S extends Solution<S,I>, I extends Instance> Constructive<S,I> nul(){
        return new NoOPConstructive<>();
    }

    /**
     * Do nothing constructive
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    private static class NullConstructive<S extends Solution<S,I>,I extends Instance> extends Constructive<S,I> {
        @Override
        public S construct(S solution) {
            return solution;
        }
    }
}
