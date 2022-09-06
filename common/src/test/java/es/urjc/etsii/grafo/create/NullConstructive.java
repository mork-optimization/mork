package es.urjc.etsii.grafo.create;


import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Do nothing constructive
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class NullConstructive<S extends Solution<S,I>,I extends Instance> extends Constructive<S,I> {

    /**
     * Create a no operation constructive method
     * Returns the solution immediately without executing any operation
     */
    @AutoconfigConstructor
    public NullConstructive() {}

    @Override
    public S construct(S solution) {
        return solution;
    }
}