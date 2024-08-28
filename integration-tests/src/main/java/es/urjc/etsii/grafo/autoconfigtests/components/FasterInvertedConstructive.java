package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;

/**
 * Test constructive method used to validate the behaviour of the autoconfig mode
 */
public class FasterInvertedConstructive extends Constructive<ACSolution, ACInstance> {

    private final double sumThis;

    /**
     * Similar to the {@link SlowConstructive}, but instead reverses the sign of the sumThis parameter.
     * @param sumThis How much to sum to the o.f in each construction
     */
    @AutoconfigConstructor
    public FasterInvertedConstructive(@RealParam(min = -50, max = 50) double sumThis) {
        this.sumThis = sumThis;
    }

    @Override
    public ACSolution construct(ACSolution solution) {
        solution.setMultiplier(-sumThis); // REVERSED
        solution.notifyUpdate();
        Metrics.add(DeclaredObjective.class, solution.getScore());
        return solution;
    }
}
