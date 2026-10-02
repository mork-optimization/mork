package es.urjc.etsii.grafo.mreflp.shake;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.mreflp.create.MREFLPReconstructionUtil;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.ArrayUtil;
import java.util.Objects;

/** Retains assignments in a fresh partial solution; never mutates or unassigns the input. */
public final class MREFLPDestructiveNew extends Destructive<MREFLPSolution, MREFLPInstance> {
    private final double fraction;
    private final RemovalPolicyNew policy;
    @AutoconfigConstructor
    public MREFLPDestructiveNew(@RealParam(min = 0.05, max = 0.3) double fraction,
                               @CategoricalParam RemovalPolicyNew policy) {
        if (!(fraction > 0 && fraction <= 1)) throw new IllegalArgumentException("Invalid destruction fraction");
        this.fraction = fraction;
        this.policy = Objects.requireNonNull(policy);
    }
    @Override public MREFLPSolution destroy(MREFLPSolution solution, int k) {
        if (k < 0) throw new IllegalArgumentException("Negative destruction intensity");
        var instance = solution.getInstance();
        int[] order = new int[instance.n()];
        for (int v = 0; v < order.length; v++) order[v] = v;
        ArrayUtil.shuffle(order);
        if (policy == RemovalPolicyNew.RELATED) {
            int seed = order[0];
            // Highest flow to the seed first; stable shuffled ties retain diversity.
            for (int i = 1; i < order.length; i++) {
                int v = order[i], j = i;
                while (j > 1 && instance.flow(seed, order[j - 1]) < instance.flow(seed, v)) {
                    order[j] = order[j - 1]; j--;
                }
                order[j] = v;
            }
        }
        int removed = (int) Math.min(instance.n(), Math.max(1, Math.ceil(fraction * instance.n() * Math.max(1, k))));
        int[] assignments = solution.assignments();
        for (int i = 0; i < removed; i++) assignments[order[i]] = -1;
        return MREFLPReconstructionUtil.fromAssignments(instance, assignments);
    }
}
