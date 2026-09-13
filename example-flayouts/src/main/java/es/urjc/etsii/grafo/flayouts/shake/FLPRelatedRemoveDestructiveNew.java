package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.ArrayList;
import java.util.Comparator;

/** Remove a seed facility and facilities related by flow, geometry, or both. */
public class FLPRelatedRemoveDestructiveNew extends Destructive<FLPSolution, FLPInstance> {
    public enum Relation { FLOW, PROXIMITY, MIXED }
    private final double ratio;
    private final double noise;
    private final Relation relation;

    @AutoconfigConstructor
    public FLPRelatedRemoveDestructiveNew(@RealParam(min = 0.02, max = 0.4) double ratio,
                                        @RealParam(min = 0, max = 1) double noise,
                                        @CategoricalParam(strings = {"FLOW", "PROXIMITY", "MIXED"}) Relation relation) {
        FLPNewUtil.probability(ratio, "ratio");
        FLPNewUtil.probability(noise, "noise");
        this.ratio = ratio;
        this.noise = noise;
        this.relation = java.util.Objects.requireNonNull(relation);
    }

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        if (solution.nAssigned() == 0 || TimeControl.isTimeUp()) return solution;
        var ranked = FLPDestructionUtil.assigned(solution);
        int seed = ranked.remove(RandomManager.getRandom().nextInt(ranked.size()));
        var centers = FLPNewUtil.centers(solution.getInstance(), FLPNewUtil.rows(solution));
        double[] score = new double[centers.length];
        for (int f : ranked) {
            double distance = Math.abs(centers[seed] - centers[f]);
            double flow = solution.getInstance().flow(seed, f);
            score[f] = switch (relation) {
                case FLOW -> flow;
                case PROXIMITY -> -distance;
                case MIXED -> flow / (1 + distance);
            };
        }
        CollectionUtil.shuffle(ranked);
        ranked.sort(Comparator.<Integer>comparingDouble(f -> score[f]).reversed());
        var removed = new ArrayList<Integer>();
        removed.add(seed);
        int count = FLPNewUtil.removalCount(solution, ratio, k, true);
        while (removed.size() < count) removed.add(FLPDestructionUtil.removeRank(ranked, noise));
        return FLPDestructionUtil.destroy(solution, removed);
    }

    @Override
    public String toString() { return "FLPRelatedRemoveDestructiveNew{ratio=" + ratio + ", noise=" + noise + ", relation=" + relation + "}"; }
}
