package es.urjc.etsii.grafo.solver.improve.sa.initialt;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Constant initial temperature calculator.
 * @param <M> Move type
 * @param <S> Your solution class
 * @param <I> Your instance class
 */
public record ConstantInitialTemperature<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance>(
        double initialTemperature) implements InitialTemperatureCalculator<M, S, I> {

    @Override
    public double initial(S solution, Neighborhood<M, S, I> neighborhood) {
        return this.initialTemperature;
    }
}
