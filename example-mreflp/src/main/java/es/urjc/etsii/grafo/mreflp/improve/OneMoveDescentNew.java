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

/** Strict relocation descent, with best or shuffled first improvement. */
public final class OneMoveDescentNew extends Improver<MREFLPSolution, MREFLPInstance> {
    private final ImprovementPolicyNew policy;
    private final boolean cached;

    @AutoconfigConstructor
    public OneMoveDescentNew(@CategoricalParam ImprovementPolicyNew policy,
                             @CategoricalParam(strings = {"true", "false"}) boolean cached) {
        super(Main.COST);
        this.policy = Objects.requireNonNull(policy);
        this.cached = cached;
    }

    @Override public MREFLPSolution improve(MREFLPSolution input) {
        var s = input.cloneSolution();
        Metrics.addCurrentObjectives(s);
        if (TimeControl.isTimeUp()) return s;
        var cache = cached ? new MREFLPDeltaCacheNew(s) : null;
        int[] facilities = new int[s.getInstance().n()], groups = new int[s.getInstance().groups()];
        for (int v = 0; v < facilities.length; v++) facilities[v] = v;
        for (int g = 0; g < groups.length; g++) groups[g] = g;
        var random = RandomManager.getRandom();
        while (!TimeControl.isTimeUp()) {
            if (policy == ImprovementPolicyNew.FIRST) {
                ArrayUtil.shuffle(facilities);
                ArrayUtil.shuffle(groups);
            }
            long bestDelta = 0;
            int chosen = -1, destination = -1, ties = 0;
            scan:
            for (int v : facilities) {
                if (TimeControl.isTimeUp()) return s;
                for (int g : groups) {
                    if (g == s.group(v) || s.occupancy(g) == s.getInstance().capacity()) continue;
                    long delta = cached ? cache.relocation(v, g) : MREFLPEvaluationUtil.relocation(s, v, g);
                    if (delta < bestDelta) {
                        bestDelta = delta; chosen = v; destination = g; ties = 1;
                        if (policy == ImprovementPolicyNew.FIRST) break scan;
                    } else if (delta < 0 && delta == bestDelta && random.nextInt(++ties) == 0) {
                        chosen = v; destination = g;
                    }
                }
            }
            if (chosen < 0 || TimeControl.isTimeUp()) break;
            int old = s.group(chosen);
            new MREFLPMove(s, chosen, destination, false, bestDelta).execute(s);
            if (cached) cache.afterRelocation(s, chosen, old);
            Metrics.addCurrentObjectives(s);
        }
        return s;
    }
}
