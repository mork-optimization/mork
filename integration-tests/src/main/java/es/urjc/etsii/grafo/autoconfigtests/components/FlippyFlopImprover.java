package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.concurrent.TimeUnit;

/**
 * Test improver used to validate the autoconfig mode
 */
public class FlippyFlopImprover extends Improver<ACSolution, ACInstance> {
    private final boolean enabled;
    private final int sleepy;

    /**
     * Build a test improver used to validate autoconfig behaviour.
     * @param enabled enables or disables the improvement. Autoconfig should choose to enable it.
     * @param sleepy how much time to sleep between improvements. Autoconfig should minimize this value.
     */
    @AutoconfigConstructor
    public FlippyFlopImprover(
            @CategoricalParam(strings = {"true", "false"}) boolean enabled,
            @CategoricalParam(strings = {"8", "6", "12", "11", "7", "5", "4", "10", "1", "9", "2", "3", "13"}) int sleepy
    ) {
        super(Context.getMainObjective());
        this.enabled = enabled;
        this.sleepy = sleepy;
    }

    @Override
    public ACSolution improve(ACSolution solution) {
        if(!enabled) return solution;
        // simulate expensive calculations
        while (!TimeControl.isTimeUp()){
            solution.setMultiplier(solution.getMultiplier() + 1);
            solution.notifyUpdate();
            Metrics.addCurrentObjectives(solution);
            ConcurrencyUtil.sleep(sleepy, TimeUnit.MILLISECONDS);
        }

        return solution;
    }
}
