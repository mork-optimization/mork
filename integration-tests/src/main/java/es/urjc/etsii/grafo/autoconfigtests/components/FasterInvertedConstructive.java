package es.urjc.etsii.grafo.autoconfigtests.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;

public class FasterInvertedConstructive extends Constructive<ACSolution, ACInstance> {

    public double sumThis;
    @AutoconfigConstructor
    public FasterInvertedConstructive(@RealParam(min = -10, max = 10) double sumThis) {
        this.sumThis = sumThis;
    }

    @Override
    public ACSolution construct(ACSolution solution) {
        solution.setScore(-sumThis); // REVERSED
        solution.notifyUpdate();
        return solution;
    }
}
