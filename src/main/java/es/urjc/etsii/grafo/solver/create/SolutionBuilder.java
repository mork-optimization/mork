package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Functional interface for generating empty solutions from a given instance.
 * Problem dependant, empty es.urjc.etsii.grafo.solution will then be filled by a constructor
 */
@FunctionalInterface
public interface SolutionBuilder<S extends Solution<I>, I extends Instance> {
    /**
     * Generate an empty es.urjc.etsii.grafo.solution with the parameters given by the user
     */
     S initializeSolution(I i);
}
