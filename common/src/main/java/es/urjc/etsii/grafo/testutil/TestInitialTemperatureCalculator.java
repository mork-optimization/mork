package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.improve.sa.initialt.InitialTemperatureCalculator;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Determines initial simulated annealing temperature.
 */
public class TestInitialTemperatureCalculator <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements InitialTemperatureCalculator<M,S,I> {
    /**
     * Determines initial simulated annealing temperature to 0.5.
     * Intended use for testing purposes
     *
     * @param solution Solution being solved
     * @param neighborhood Current neighborhood
     * @return temperature as a double
     */
    public double initial(S solution, Neighborhood<M, S, I> neighborhood){
        return Integer.MAX_VALUE;
    }
}
