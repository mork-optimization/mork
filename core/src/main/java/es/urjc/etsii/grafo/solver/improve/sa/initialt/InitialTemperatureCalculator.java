package es.urjc.etsii.grafo.solver.improve.sa.initialt;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Determines initial simulated annealing temperature
 */
@FunctionalInterface
public interface InitialTemperatureCalculator<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {
    /**
     * Determines initial simulated annealing temperature
     *
     * @param solution Solution being solved
     * @param neighborhood Current neighborhood
     * @return temperature as a double
     */
    double initial(S solution, Neighborhood<M, S, I> neighborhood);
}
