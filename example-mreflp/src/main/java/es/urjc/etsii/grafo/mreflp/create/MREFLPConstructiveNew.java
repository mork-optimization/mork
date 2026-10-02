package es.urjc.etsii.grafo.mreflp.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mreflp.alg.LearningMatrixNew;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import java.util.Objects;

/** Separate constructive variant with maintained marginal costs, RCLs and flow ordering. */
public final class MREFLPConstructiveNew extends Constructive<MREFLPSolution, MREFLPInstance> {
    private final ConstructionPolicyNew policy;
    private final FacilityOrderNew order;
    private final double rcl;

    @AutoconfigConstructor
    public MREFLPConstructiveNew(@CategoricalParam ConstructionPolicyNew policy,
                                @CategoricalParam FacilityOrderNew order,
                                @RealParam(min = 0, max = 1) double rcl) {
        if (!(rcl >= 0 && rcl <= 1)) throw new IllegalArgumentException("Invalid RCL fraction");
        this.policy = Objects.requireNonNull(policy);
        this.order = Objects.requireNonNull(order);
        this.rcl = rcl;
    }

    @Override public MREFLPSolution construct(MREFLPSolution solution) {
        return construct(solution, null, 0);
    }

    public MREFLPSolution construct(MREFLPSolution solution, LearningMatrixNew learning, double epsilon) {
        if (!(epsilon >= 0 && epsilon <= 1)) throw new IllegalArgumentException("Invalid epsilon");
        return MREFLPReconstructionUtil.complete(solution, policy, order, rcl, learning, epsilon);
    }
}
