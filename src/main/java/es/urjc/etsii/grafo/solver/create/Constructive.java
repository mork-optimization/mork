package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public abstract class Constructive<S extends Solution<I>, I extends Instance> {

    /**
     * Initialize a es.urjc.etsii.grafo.solution using any of the available strategies
     * @param s Specify how a es.urjc.etsii.grafo.solution is initialized from an instance
     * @return A valid es.urjc.etsii.grafo.solution that fulfills all the problem constraints
     */
    public abstract S construct(S s);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
