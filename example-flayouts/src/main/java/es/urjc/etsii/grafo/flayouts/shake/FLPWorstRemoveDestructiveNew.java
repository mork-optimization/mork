package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.ArrayList;
import java.util.Comparator;

/** Rank by current pairwise cost contribution, a proxy rather than the marginal removal delta. */
public class FLPWorstRemoveDestructiveNew extends Destructive<FLPSolution, FLPInstance> {
    private final double ratio;
    private final double noise;

    @AutoconfigConstructor
    public FLPWorstRemoveDestructiveNew(@RealParam(min = 0.02, max = 0.4) double ratio,
                                      @RealParam(min = 0, max = 1) double noise) {
        FLPNewUtil.probability(ratio, "ratio");
        FLPNewUtil.probability(noise, "noise");
        this.ratio = ratio;
        this.noise = noise;
    }

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        if (solution.nAssigned() == 0 || TimeControl.isTimeUp()) return solution;
        var ranked = FLPDestructionUtil.assigned(solution);
        double[] center = FLPNewUtil.centers(solution.getInstance(), FLPNewUtil.rows(solution));
        double[] cost = new double[center.length];
        for (int f : ranked) {
            if (TimeControl.isTimeUp()) return solution;
            for (int g : ranked) cost[f] += Math.abs(center[f] - center[g]) * solution.getInstance().flow(f, g);
        }
        CollectionUtil.shuffle(ranked);
        ranked.sort(Comparator.<Integer>comparingDouble(f -> cost[f]).reversed());
        int count = FLPNewUtil.removalCount(solution, ratio, k, true);
        var removed = new ArrayList<Integer>();
        while (removed.size() < count) removed.add(FLPDestructionUtil.removeRank(ranked, noise));
        return FLPDestructionUtil.destroy(solution, removed);
    }

    @Override
    public String toString() { return "FLPWorstRemoveDestructiveNew{ratio=" + ratio + ", noise=" + noise + "}"; }
}
