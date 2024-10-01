package es.urjc.etsii.grafo.improve.sa.initialt;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Calculate initial temperature as maximum difference between movements in neighborhood.
 *
 * @param <M> Move type
 * @param <S> Your solution class
 * @param <I> Your instance class
 */
public class MaxDifferenceInitialTemperature<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements InitialTemperatureCalculator<M,S,I> {

    private final double ratio;
    private final Objective<M,S,I> objective;

    /**
     * <p>Constructor for MaxDifferenceInitialTemperature.</p>
     *
     * @param ratio a double.
     */
    public MaxDifferenceInitialTemperature(Objective<M,S,I> objective, double ratio) {
        this.objective = objective;
        this.ratio = ratio;
    }
    /**
     * <p>Constructor for MaxDifferenceInitialTemperature.</p>
     */
    public MaxDifferenceInitialTemperature(Objective<M,S,I> objective) {
        this(objective, 1D);
    }

    /** {@inheritDoc} */
    @Override
    public double initial(S solution, Neighborhood<M, S, I> neighborhood) {
        var summary = neighborhood.explore(solution).moves().mapToDouble(objective::evalMove).summaryStatistics();
        double diff = Math.abs(summary.getMin() - summary.getMax());
        double initialTemperature = diff * this.ratio;
        return initialTemperature;
    }
}
