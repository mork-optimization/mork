package es.urjc.etsii.grafo.mreflp.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Original tabu trajectory with the optimized delta cache and explicit incumbent metrics. */
public final class OneMoveTabuSearchNew extends Improver<MREFLPSolution, MREFLPInstance> {
    private final int maxIter, tenure;
    private final boolean cached;
    @AutoconfigConstructor
    public OneMoveTabuSearchNew(@IntegerParam(min = 1, max = 1_000) int maxIter,
                            @IntegerParam(min = 1, max = 100) int tenure,
                            @CategoricalParam(strings = {"true", "false"}) boolean cached) {
        super(Main.COST);
        if (maxIter < 1 || tenure < 1) throw new IllegalArgumentException("Invalid tabu parameters");
        this.maxIter = maxIter;
        this.tenure = tenure;
        this.cached = cached;
    }

    public static boolean admissible(long candidateCost, long bestCost, long iteration, long expiry) {
        return iteration >= expiry || candidateCost < bestCost;
    }

    @Override public MREFLPSolution improve(MREFLPSolution input) {
        var s = input.cloneSolution();
        var best = input.cloneSolution();
        Metrics.addCurrentObjectives(best);
        if (TimeControl.isTimeUp()) return best;
        var cache = cached ? new MREFLPDeltaCacheNew(s) : null;
        long[] tabu = new long[s.getInstance().n()];
        long iteration = 0;
        int stagnant = 0;
        var random = RandomManager.getRandom();
        while (stagnant < maxIter && !TimeControl.isTimeUp()) {
            long bestDelta = Long.MAX_VALUE;
            int facility = -1, group = -1, ties = 0;
            boolean feasible = false;
            for (int v = 0; v < tabu.length; v++) {
                if (TimeControl.isTimeUp()) return best;
                for (int g = 0; g < s.getInstance().groups(); g++) if (g != s.group(v) && s.occupancy(g) < s.getInstance().capacity()) {
                    feasible = true;
                    long delta = cached ? cache.relocation(v, g) : MREFLPEvaluationUtil.relocation(s, v, g);
                    if (!admissible(s.cost() + delta, best.cost(), iteration, tabu[v])) continue;
                    if (delta < bestDelta) { bestDelta = delta; facility = v; group = g; ties = 1; }
                    else if (delta == bestDelta && random.nextInt(++ties) == 0) { facility = v; group = g; }
                }
            }
            if (!feasible) break;
            iteration++;
            if (facility < 0) { stagnant++; continue; }
            if (TimeControl.isTimeUp()) break;
            int old = s.group(facility);
            new MREFLPMove(s, facility, group, false, bestDelta).execute(s);
            if (cached) cache.afterRelocation(s, facility, old);
            tabu[facility] = iteration + random.nextInt(1, tenure + 1);
            if (s.cost() < best.cost()) {
                best = s.cloneSolution();
                stagnant = 0;
                Metrics.addCurrentObjectives(best);
            }
            else stagnant++;
        }
        return best;
    }
}
