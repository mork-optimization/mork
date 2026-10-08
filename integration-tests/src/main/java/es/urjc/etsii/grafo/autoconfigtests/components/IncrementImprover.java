package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;

public class IncrementImprover extends Improver<ACSolution, ACInstance> {

    @AutoconfigConstructor
    public IncrementImprover() {
        super(Context.getMainObjective());
    }

    @Override
    public ACSolution improve(ACSolution solution) {
        solution.setMultiplier(solution.getMultiplier() + 1);
        solution.notifyUpdate();
        Metrics.addCurrentObjectives(solution);
        return solution;
    }
}
