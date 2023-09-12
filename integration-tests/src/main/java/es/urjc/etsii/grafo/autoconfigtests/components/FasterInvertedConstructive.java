package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.Metrics;

public class FasterInvertedConstructive extends Constructive<ACSolution, ACInstance> {

    public double sumThis;
    @AutoconfigConstructor
    public FasterInvertedConstructive(@RealParam(min = -50, max = 50) double sumThis) {
        this.sumThis = sumThis;
    }

    @Override
    public ACSolution construct(ACSolution solution) {
        solution.setMultiplier(-sumThis); // REVERSED
        solution.notifyUpdate();
        Metrics.add(BestObjective.class, solution.getScore());
        return solution;
    }
}
