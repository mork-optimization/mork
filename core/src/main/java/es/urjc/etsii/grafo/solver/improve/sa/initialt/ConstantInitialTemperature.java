package es.urjc.etsii.grafo.solver.improve.sa.initialt;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Constant initial temperature calculator
 * @param <M> Move type
 * @param <S> Your solution class
 * @param <I> Your instance class
 */
public class ConstantInitialTemperature<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements InitialTemperatureCalculator<M,S,I>{

    private final double initialTemperature;

    public ConstantInitialTemperature(double initialTemperature) {
        this.initialTemperature = initialTemperature;
    }

    @Override
    public double initial(S solution, Neighborhood<M, S, I> neighborhood) {
        return this.initialTemperature;
    }
}
