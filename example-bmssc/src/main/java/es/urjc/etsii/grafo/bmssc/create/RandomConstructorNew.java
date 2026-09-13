package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;

/** Copy of RandomConstructor that also fills the missing quotas of a partial solution. */
public class RandomConstructorNew extends Reconstructive<BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public RandomConstructorNew() {}

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        BMSSCNewUtil.requireEmpty(solution);
        return reconstruct(solution);
    }

    @Override
    public BMSSCSolution reconstruct(BMSSCSolution solution) {
        BMSSCUtil.withPartialSolution(() -> {
            BMSSCNewUtil.fillQuotas(solution, true);
            return solution;
        });
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        return solution;
    }
}
