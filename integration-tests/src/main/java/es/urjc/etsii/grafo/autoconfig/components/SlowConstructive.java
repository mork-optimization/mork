package es.urjc.etsii.grafo.autoconfig.components;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.autoconfig.model.ACInstance;
import es.urjc.etsii.grafo.autoconfig.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

public class SlowConstructive extends Constructive<ACSolution, ACInstance> {

    public int sumThis;
    @AutoconfigConstructor
    public SlowConstructive(@IntegerParam(min = -10, max = 10) int sumThis) {
        this.sumThis = sumThis;
    }

    @Override
    public ACSolution construct(ACSolution solution) {
        ConcurrencyUtil.sleep(5, TimeUnit.MILLISECONDS);
        solution.setScore(sumThis);
        solution.notifyUpdate();
        return solution;
    }
}
