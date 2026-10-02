package es.urjc.etsii.grafo.mreflp.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.mreflp.neighborhood.MREFLPNeighborhoodUtil;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.TimeControl;

/** Always-feasible perturbation; k=0 already applies a positive number of moves. */
public final class MREFLPShakeNew extends Shake<MREFLPSolution, MREFLPInstance> {
    private final double strength, relocationProbability;
    @AutoconfigConstructor
    public MREFLPShakeNew(@RealParam(min = 0.01, max = 0.2) double strength,
                         @RealParam(min = 0, max = 1) double relocationProbability) {
        if (!(strength > 0 && strength <= 1) || !(relocationProbability >= 0 && relocationProbability <= 1)) {
            throw new IllegalArgumentException("Invalid shake parameters");
        }
        this.strength = strength;
        this.relocationProbability = relocationProbability;
    }
    @Override public MREFLPSolution shake(MREFLPSolution solution, int k) {
        if (k < 0) throw new IllegalArgumentException("Negative shake intensity");
        int moves = (int) Math.min(solution.getInstance().n(), Math.max(1,
                Math.ceil(strength * solution.getInstance().n() * ((long) k + 1))));
        for (int step = 0; step < moves && !TimeControl.isTimeUp(); step++) {
            var move = MREFLPNeighborhoodUtil.randomMixed(solution, relocationProbability);
            if (move.isEmpty()) break;
            move.get().execute(solution);
        }
        return solution;
    }
}
