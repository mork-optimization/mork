package es.urjc.etsii.grafo.solver.improve.sa;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Determines when the Simulated Annealing stops
 */
@FunctionalInterface
public interface TerminationCriteria<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {
    /**
     * <p>terminate.</p>
     *
     * @param solution a S object.
     * @param neighborhood a {@link es.urjc.etsii.grafo.solution.neighborhood.Neighborhood} object.
     * @param currentTemperature a double.
     * @param iteration a int.
     * @return a boolean.
     */
    boolean terminate(S solution, Neighborhood<M, S, I> neighborhood, double currentTemperature, int iteration);
}
