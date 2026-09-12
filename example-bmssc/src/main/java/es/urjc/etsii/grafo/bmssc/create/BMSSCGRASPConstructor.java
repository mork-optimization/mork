package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;

public class BMSSCGRASPConstructor extends Constructive<BMSSCSolution, BMSSCInstance> {
    private final double alpha;
    private final Constructive<BMSSCSolution, BMSSCInstance> delegate;

    @AutoconfigConstructor
    public BMSSCGRASPConstructor(@RealParam(min = 0, max = 1) double alpha) {
        this(alpha, new BMSSCListManager());
    }

    public BMSSCGRASPConstructor(double alpha, BMSSCListManager listManager) {
        if (!Double.isFinite(alpha) || alpha < 0 || alpha > 1) throw new IllegalArgumentException("Invalid alpha: " + alpha);
        this.alpha = alpha;
        this.delegate = new GraspBuilder<BMSSCMove, BMSSCSolution, BMSSCInstance>()
                .withStrategyGreedyRandom().withObjective(Main.OBJ)
                .withAlphaValue(alpha).withListManager(listManager).build();
    }

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        var result = BMSSCUtil.withPartialSolution(() -> delegate.construct(solution));
        if (!result.feasibleClusterSizes()) throw new IllegalStateException("Construction did not fill cluster quotas");
        assert Context.validate(result);
        Metrics.addCurrentObjectives(result);
        return result;
    }

    @Override
    public String toString() { return "BMSSCGRASPConstructor{alpha=" + alpha + "}"; }
}
