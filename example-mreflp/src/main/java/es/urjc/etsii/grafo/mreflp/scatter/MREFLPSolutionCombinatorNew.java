package es.urjc.etsii.grafo.mreflp.scatter;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import es.urjc.etsii.grafo.util.TimeControl;

public final class MREFLPSolutionCombinatorNew extends SolutionCombinator<MREFLPSolution, MREFLPInstance> {
    private final double parentBias, rcl;
    @AutoconfigConstructor
    public MREFLPSolutionCombinatorNew(@RealParam(min = 0, max = 1) double parentBias,
                                      @RealParam(min = 0, max = 1) double rcl) {
        if (!(parentBias >= 0 && parentBias <= 1) || !(rcl >= 0 && rcl <= 1)) throw new IllegalArgumentException("Invalid combination parameters");
        this.parentBias = parentBias; this.rcl = rcl;
    }
    @Override public List<MREFLPSolution> apply(MREFLPSolution left, MREFLPSolution right) {
        return List.of(MREFLPCombinationUtil.combine(left, right, parentBias, rcl),
                MREFLPCombinationUtil.combine(right, left, parentBias, rcl));
    }

    @Override public Set<MREFLPSolution> newSet(MREFLPSolution[] currentSet, Set<MREFLPSolution> newSolutions) {
        var combined = new LinkedHashSet<MREFLPSolution>();
        for (var solution : newSolutions) for (var reference : currentSet) {
            if (TimeControl.isTimeUp()) return combined;
            combined.addAll(apply(solution, reference));
        }
        return combined;
    }
}
