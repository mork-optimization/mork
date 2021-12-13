package es.urjc.etsii.grafo.solver.improve.sa.cd;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Specify how the temperature changes in each simulated annealing iteration.
 * Given current iteration and temperature set next iteration temperature.
 * It is recommended to use a lambda expression instead of extending this interface directly.
 */
@FunctionalInterface
public interface CoolDownControl<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {
    /**
     * Set temperature for the next iteration of simulated annealing
     *
     * @param solution current best solution
     * @param neighborhood neighborhood used in the SA
     * @param currentTemperature current temperature (before cooldown)
     * @param iteration current iteration
     * @return new temperature
     */
    double coolDown(S solution, Neighborhood<M, S, I> neighborhood, double currentTemperature, int iteration);
}
