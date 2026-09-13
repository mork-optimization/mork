package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;

/** Corrected random-removal copy with compacted survivors and optional VNS strength scaling. */
public class RandomRemoveDestructiveNew extends Destructive<FLPSolution, FLPInstance> {
    private final double ratio;
    private final boolean scaleWithK;
    private final FLPRemoveNeighNew neighborhood;

    @AutoconfigConstructor
    public RandomRemoveDestructiveNew(FLPRemoveNeighNew neighborhood, @RealParam(min = 0.02, max = 0.4) double ratio,
                                     @CategoricalParam(strings = {"false", "true"}) boolean scaleWithK) {
        FLPNewUtil.probability(ratio, "ratio");
        this.ratio = ratio;
        this.scaleWithK = scaleWithK;
        this.neighborhood = java.util.Objects.requireNonNull(neighborhood);
    }

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        if (TimeControl.isTimeUp()) return solution;
        var ids = FLPDestructionUtil.assigned(solution);
        CollectionUtil.shuffle(ids);
        int count = FLPNewUtil.removalCount(solution, ratio, k, scaleWithK);
        var copy = solution.cloneSolution();
        if (count > 0) {
            int[] removed = new int[count];
            for (int i = 0; i < count; i++) removed[i] = ids.get(i);
            neighborhood.removeAll(copy, removed).execute(copy);
        }
        return copy;
    }

    @Override
    public String toString() { return "RandomRemoveDestructiveNew{ratio=" + ratio + ", scaleWithK=" + scaleWithK + "}"; }
}
