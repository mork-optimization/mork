package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.ConstructiveNeighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public abstract class Constructor<S extends Solution> {

    /**
     * Initialize a es.urjc.etsii.grafo.solution using any of the available strategies
     * @param builder Specify how a es.urjc.etsii.grafo.solution is initialized from an instance
     * @return A valid es.urjc.etsii.grafo.solution that fulfills all the problem constraints
     */
    public abstract S construct(Instance i, SolutionBuilder builder, ConstructiveNeighborhood neighborhood);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
