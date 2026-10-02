package es.urjc.etsii.grafo.mreflp.create;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.mreflp.model.*;
import java.util.Objects;

/** Completes only missing assignments, retaining all other facility locations. */
public final class MREFLPReconstructiveNew extends Reconstructive<MREFLPSolution, MREFLPInstance> {
    private final ConstructionPolicyNew policy;
    private final FacilityOrderNew order;
    private final double rcl;
    @AutoconfigConstructor
    public MREFLPReconstructiveNew(@CategoricalParam ConstructionPolicyNew policy,
                                  @CategoricalParam FacilityOrderNew order,
                                  @RealParam(min = 0, max = 1) double rcl) {
        if (!(rcl >= 0 && rcl <= 1)) throw new IllegalArgumentException("Invalid RCL fraction");
        this.policy = Objects.requireNonNull(policy);
        this.order = Objects.requireNonNull(order);
        this.rcl = rcl;
    }
    @Override public MREFLPSolution construct(MREFLPSolution solution) { return reconstruct(solution); }
    @Override public MREFLPSolution reconstruct(MREFLPSolution solution) {
        return MREFLPReconstructionUtil.complete(solution.cloneSolution(), policy, order, rcl, null, 0);
    }
}
