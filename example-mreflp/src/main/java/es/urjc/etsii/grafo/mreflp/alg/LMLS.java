package es.urjc.etsii.grafo.mreflp.alg;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructive;
import es.urjc.etsii.grafo.mreflp.improve.OneMoveTabuSearch;
import es.urjc.etsii.grafo.mreflp.improve.SwapDescent;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.Map;

/** Algorithm 1. Configuration is immutable; all learned state is allocated inside algorithm(). */
public final class LMLS extends Algorithm<MREFLPSolution, MREFLPInstance> {
    private final LMLSVariant variant;
    private final double epsilon, alpha, beta, gamma, rho;
    private final int maxIter, tenure;
    private final int maxRestarts;
    public LMLS(LMLSVariant variant, double epsilon, int maxIter, int tenure,
                double alpha, double beta, double gamma, double rho, int maxRestarts) {
        super(variant.paperName());
        if (!(epsilon >= 0 && epsilon <= 1) || maxIter < 1 || tenure < 1) throw new IllegalArgumentException("Invalid search parameters");
        for (double factor : new double[]{alpha, beta, gamma, rho}) {
            if (!(factor > 0 && factor < 1)) throw new IllegalArgumentException("Learning factors must be in (0,1)");
        }
        if (maxRestarts < 0) throw new IllegalArgumentException("Negative restart limit");
        this.variant = variant;
        this.epsilon = epsilon;
        this.maxIter = maxIter;
        this.tenure = tenure;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.rho = rho;
        this.maxRestarts = maxRestarts;
    }

    public Map<String, Number> parameters() {
        return Map.of("epsilon", epsilon, "maxIter", maxIter, "tenure", tenure,
                "alpha", alpha, "beta", beta, "gamma", gamma, "rho", rho);
    }

    @Override public MREFLPSolution algorithm(MREFLPInstance instance) {
        if (!TimeControl.isEnabled() && maxRestarts == 0) throw new IllegalStateException("LMLS needs a time budget or a positive restart limit");
        var learning = variant.learns() ? new LearningMatrix(instance.n(), instance.groups(), alpha, beta, gamma, rho) : null;
        var constructive = new MREFLPConstructive(learning, variant, epsilon);
        var tabu = new OneMoveTabuSearch(maxIter, tenure, variant != LMLSVariant.DIRECT_ONE_MOVE);
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
