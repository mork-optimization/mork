package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.flayouts.constructives.FLPConstructionUtil;
import java.util.List;
import java.util.Objects;

/** Rebuilding list manager with an injected insertion neighborhood and deadline completion. */
public class FLPAddListManagerNew extends GRASPListManager<FLPAddNeigh.AddMove, FLPSolution, FLPInstance> {
    private final FLPAddNeigh neighborhood;
    @AutoconfigConstructor
    public FLPAddListManagerNew(FLPAddNeigh neighborhood) { this.neighborhood = Objects.requireNonNull(neighborhood); }

    @Override
    public List<FLPAddNeigh.AddMove> buildInitialCandidateList(FLPSolution solution) {
        return FLPConstructionUtil.insertionCandidates(solution, neighborhood);
    }

    @Override
    public List<FLPAddNeigh.AddMove> updateCandidateList(FLPSolution solution, FLPAddNeigh.AddMove move, List<FLPAddNeigh.AddMove> candidateList, int index) {
        return buildInitialCandidateList(solution);
    }

    @Override
    public String toString() { return "FLPAddListManagerNew{" + neighborhood + "}"; }
}
