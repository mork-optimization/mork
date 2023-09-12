package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.concurrent.TimeUnit;

public class FlippyFlopImprover extends Improver<ACSolution, ACInstance> {
    private final boolean enabled;
    private final int sleepy;

    /**
     * Initialize common improver fields, to be called by subclasses
     *
     * @param ofmode MAXIMIZE to maximize scores returned by the given move, MINIMIZE for minimizing
     */
    @AutoconfigConstructor
    public FlippyFlopImprover(
            @ProvidedParam FMode ofmode,
            @CategoricalParam(strings = {"true", "false"}) boolean enabled,
            @CategoricalParam(strings = {"8", "6", "12", "11", "7", "5", "4", "10", "1", "9", "2", "3", "13"}) int sleepy
    ) {
        super(ofmode);
        this.enabled = enabled;
        this.sleepy = sleepy;
    }

    @Override
    protected ACSolution _improve(ACSolution solution) {
        if(!enabled) return solution;
        // simulate expensive calculations
        while (!TimeControl.isTimeUp()){
            solution.setMultiplier(solution.getMultiplier() + 1);
            solution.notifyUpdate();
            Metrics.add(BestObjective.class, solution.getScore());
            ConcurrencyUtil.sleep(sleepy, TimeUnit.MILLISECONDS);
        }

        return solution;
    }
}
