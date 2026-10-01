package es.urjc.etsii.grafo.mreflp.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mreflp.alg.LearningMatrix;
import es.urjc.etsii.grafo.mreflp.alg.LMLSVariant;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Algorithm 2, plus the two construction ablations. */
public final class MREFLPConstructive extends Constructive<MREFLPSolution, MREFLPInstance> {
    private final LMLSVariant variant;
    private final double epsilon;
    @AutoconfigConstructor
    public MREFLPConstructive(@CategoricalParam(strings = {"LMLS", "RANDOM", "GREEDY"}) LMLSVariant variant,
                             @RealParam(min = 0, max = 1) double epsilon) {
        if (!(epsilon >= 0 && epsilon <= 1)) throw new IllegalArgumentException("Invalid epsilon");
        this.variant = variant;
        this.epsilon = epsilon;
    }

    @Override public MREFLPSolution construct(MREFLPSolution s) {
        return construct(s, null);
    }

    /** LMLS supplies its run-local learning matrix; standalone construction starts with uniform weights. */
    public MREFLPSolution construct(MREFLPSolution s, LearningMatrix learning) {
        var random = RandomManager.getRandom();
        int n = s.getInstance().n(), k = s.getInstance().groups();
        int[] order = new int[n];
        for (int v = 0; v < n; v++) order[v] = v;
        ArrayUtil.shuffle(order);
        // Always finish a feasible construction, including when a tiny budget expires mid-construction.
        for (int v : order) {
            boolean greedy = variant == LMLSVariant.GREEDY;
            boolean informed = !greedy && variant.learns() && random.nextDouble() < epsilon;
            double bestValue = -Double.MAX_VALUE;
            int chosen = -1, ties = 0;
            for (int g = 0; g < k; g++) if (s.occupancy(g) < s.getInstance().capacity()) {
                double value = 0;
                if (greedy) {
                    long marginal = 0;
                    for (int u = 0; u < n; u++) if (s.group(u) >= 0) marginal += s.getInstance().flow(u, v) * Math.abs(g - s.group(u));
                    value = -marginal;
                } else if (informed && learning != null) value = learning.value(v, g);
                if (value > bestValue) { bestValue = value; chosen = g; ties = 1; }
                else if (value == bestValue && random.nextInt(++ties) == 0) chosen = g;
            }
            s.assign(v, chosen);
        }
        s.notifyUpdate();
        return s;
    }
}
