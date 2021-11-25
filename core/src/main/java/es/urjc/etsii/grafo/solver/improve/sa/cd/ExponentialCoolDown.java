package es.urjc.etsii.grafo.solver.improve.sa.cd;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Exponential coolDown strategy
 *
 * @param <M> Move type
 * @param <S> Your solution class
 * @param <I> Your instance class
 */
public class ExponentialCoolDown<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements CoolDownControl<M,S,I>{

    private final double ratio;

    /**
     * Create a new ExponentialCoolDown with the provided cooldown ratio. NewTemperature = OldTemperature * ratio
     *
     * @param ratio cooldown ratio.
     */
    public ExponentialCoolDown(double ratio){
        this.ratio = ratio;
    }

    /** {@inheritDoc} */
    @Override
    public double coolDown(S solution, Neighborhood<M, S, I> neighborhood, double currentTemperature, int iteration) {
        return currentTemperature * ratio;
    }
}
