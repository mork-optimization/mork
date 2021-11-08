package es.urjc.etsii.grafo.solver.improve.sa.initialt;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Calculate initial temperature as maximum difference between movements in neighborhood.
 * @param <M>
 * @param <S>
 * @param <I>
 */
public class MaxDifferenceInitialTemperature<M extends Move<S,I>, S extends Solution<I>, I extends Instance> implements InitialTemperatureCalculator<M,S,I>{

    private final double ratio;

    public MaxDifferenceInitialTemperature(double ratio) {
        this.ratio = ratio;
    }
    public MaxDifferenceInitialTemperature() {
        this(1D);
    }

    @Override
    public double initial(S solution, Neighborhood<M, S, I> neighborhood) {
        var summary = neighborhood.stream(solution).mapToDouble(Move::getValue).summaryStatistics();
        double diff = Math.abs(summary.getMin() - summary.getMax());
        double initialTemperature = diff * this.ratio;
        return initialTemperature;
    }
}
