package es.urjc.etsii.grafo.mreflp.alg;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructive;
import es.urjc.etsii.grafo.mreflp.improve.OneMoveTabuSearch;
import es.urjc.etsii.grafo.mreflp.improve.SwapDescent;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;

/** Algorithm 1. Configuration is immutable; all learned state is allocated inside algorithm(). */
public final class LMLS extends Algorithm<MREFLPSolution, MREFLPInstance> {
    private final LMLSVariant variant;
    private final LMLSParameters parameters;
    private final int maxRestarts;
    public LMLS(LMLSVariant variant, LMLSParameters parameters, int maxRestarts) {
        super(variant.paperName());
        if (maxRestarts < 0) throw new IllegalArgumentException("Negative restart limit");
        this.variant = variant;
        this.parameters = parameters;
        this.maxRestarts = maxRestarts;
    }

    @Override public MREFLPSolution algorithm(MREFLPInstance instance) {
        if (!TimeControl.isEnabled() && maxRestarts == 0) throw new IllegalStateException("LMLS needs a time budget or a positive restart limit");
        var learning = variant.learns() ? new LearningMatrix(instance.n(), instance.groups(), parameters) : null;
        var constructive = new MREFLPConstructive(learning, variant, parameters.epsilon());
        var tabu = new OneMoveTabuSearch(parameters, variant != LMLSVariant.DIRECT_ONE_MOVE);
        var swap = new SwapDescent(variant != LMLSVariant.DIRECT_SWAP);
        MREFLPSolution best = null;
        int restarts = 0;
        do {
            var solution = constructive.construct(newSolution(instance));
            int[] initial = learning == null ? null : solution.assignments();
            if (variant != LMLSVariant.WITHOUT_TABU) solution = tabu.improve(solution);
            if (variant != LMLSVariant.WITHOUT_SWAP) solution = swap.improve(solution);
            if (best == null || solution.cost() < best.cost()) {
                best = solution.cloneSolution();
                if (Metrics.areMetricsEnabled()) Metrics.addCurrentObjectives(best);
            }
            if (learning != null && !TimeControl.isTimeUp()) learning.update(initial, solution);
            restarts++;
        } while (!TimeControl.isTimeUp() && (maxRestarts == 0 || restarts < maxRestarts));
        return best;
    }
}
