package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

/**
 * Test constructive method used to validate the behaviour of the autoconfig mode
 */
public class SlowConstructive extends Constructive<ACSolution, ACInstance> {

    private final int sumThis;
    @AutoconfigConstructor
    public SlowConstructive(@IntegerParam(min = -10, max = 10) int sumThis) {
        this.sumThis = sumThis;
    }

    @Override
    public ACSolution construct(ACSolution solution) {
        solution.setMultiplier(sumThis);
        solution.notifyUpdate();
        Metrics.add(DeclaredObjective.class, solution.getScore());
        ConcurrencyUtil.sleep(5, TimeUnit.MILLISECONDS); // Simulate slow constructive, take time from the improver method
        return solution;
    }
}
