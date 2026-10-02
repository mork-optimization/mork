package es.urjc.etsii.grafo.mreflp.improve;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.Objects;

/** Copy of swap descent with a new cache, shuffled first improvement and optional sampling. */
public final class SwapDescentNew extends Improver<MREFLPSolution, MREFLPInstance> {
    private final ImprovementPolicyNew policy;
    private final boolean cached;
    private final double sampleFraction;

    @AutoconfigConstructor
    public SwapDescentNew(@CategoricalParam ImprovementPolicyNew policy,
                          @CategoricalParam(strings = {"true", "false"}) boolean cached,
                          @RealParam(min = 0.05, max = 1) double sampleFraction) {
        super(Main.COST);
        if (!(sampleFraction > 0 && sampleFraction <= 1)) throw new IllegalArgumentException("Invalid sampling fraction");
        this.policy = Objects.requireNonNull(policy);
        this.cached = cached;
        this.sampleFraction = sampleFraction;
    }

    @Override public MREFLPSolution improve(MREFLPSolution input) {
        var s = input.cloneSolution();
        Metrics.addCurrentObjectives(s);
        if (TimeControl.isTimeUp()) return s;
        var cache = cached ? new MREFLPDeltaCacheNew(s) : null;
        int[] order = new int[s.getInstance().n()];
        for (int v = 0; v < order.length; v++) order[v] = v;
        var random = RandomManager.getRandom();
        while (!TimeControl.isTimeUp()) {
            if (policy == ImprovementPolicyNew.FIRST) ArrayUtil.shuffle(order);
            long bestDelta = 0;
            int first = -1, second = -1, ties = 0;
            scan:
            for (int a = 0; a < order.length; a++) {
                if (TimeControl.isTimeUp()) return s;
                for (int b = a + 1; b < order.length; b++) {
                    int u = order[a], v = order[b];
                    if (s.group(u) == s.group(v)) continue;
                    if (sampleFraction < 1 && random.nextDouble() >= sampleFraction) continue;
                    long delta = cached ? cache.swap(s, u, v) : MREFLPEvaluationUtil.swap(s, u, v);
                    if (delta < bestDelta) {
                        bestDelta = delta; first = u; second = v; ties = 1;
                        if (policy == ImprovementPolicyNew.FIRST) break scan;
                    } else if (delta < 0 && delta == bestDelta && random.nextInt(++ties) == 0) {
                        first = u; second = v;
                    }
                }
            }
            if (first < 0 || TimeControl.isTimeUp()) break;
            int oldU = s.group(first), oldV = s.group(second);
            new MREFLPMove(s, first, second, true, bestDelta).execute(s);
            if (cached) cache.afterSwap(s, first, second, oldU, oldV);
            Metrics.addCurrentObjectives(s);
        }
        return s;
    }
}
