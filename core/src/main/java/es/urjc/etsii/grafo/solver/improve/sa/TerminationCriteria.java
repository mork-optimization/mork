package es.urjc.etsii.grafo.solver.improve.sa;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Determines when the Simulated Annealing stops
 */
@FunctionalInterface
public interface TerminationCriteria<M extends Move<S,I>, S extends Solution<I>, I extends Instance> {
    boolean terminate(S solution, Neighborhood<M, S, I> neighborhood, double currentTemperature, int iteration);
}
