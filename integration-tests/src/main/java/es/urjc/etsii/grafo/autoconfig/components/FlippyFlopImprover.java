package es.urjc.etsii.grafo.autoconfig.components;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.OrdinalParam;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.autoconfig.model.ACInstance;
import es.urjc.etsii.grafo.autoconfig.model.ACSolution;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;

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
            @OrdinalParam(strings = {"1", "2", "3", "5"}) int sleepy
    ) {
        super(ofmode);
        this.enabled = enabled;
        this.sleepy = sleepy;
    }

    @Override
    protected ACSolution _improve(ACSolution solution) {
        if(!enabled) return solution;
        // simulate expensive calculations
        ConcurrencyUtil.sleep(sleepy, TimeUnit.MILLISECONDS);
        solution.setScore(solution.getScore() + 1);
        solution.notifyUpdate();
        return solution;
    }
}
