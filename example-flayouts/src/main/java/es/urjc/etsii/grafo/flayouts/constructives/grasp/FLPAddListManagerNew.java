package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.List;
import java.util.Objects;

/** Copy of the rebuilding list manager using an injected corrected insertion neighborhood. */
public class FLPAddListManagerNew extends GRASPListManager<FLPNewMove, FLPSolution, FLPInstance> {
    private final FLPAddNeighNew neighborhood;
    @AutoconfigConstructor
    public FLPAddListManagerNew(FLPAddNeighNew neighborhood) { this.neighborhood = Objects.requireNonNull(neighborhood); }

    @Override
    public List<FLPNewMove> buildInitialCandidateList(FLPSolution solution) {
        if (solution.getNotAssignedFacilities().isEmpty()) return List.of();
        if (TimeControl.isTimeUp()) return List.of(FLPNewUtil.appendMissing(solution));
        var moves = neighborhood.exploreList(solution);
        // If exploration reached the deadline before producing a move, still complete repair.
        return moves.isEmpty() ? List.of(FLPNewUtil.appendMissing(solution)) : moves;
    }

    @Override
    public List<FLPNewMove> updateCandidateList(FLPSolution solution, FLPNewMove move, List<FLPNewMove> candidateList, int index) {
        return buildInitialCandidateList(solution);
    }

    @Override
    public String toString() { return "FLPAddListManagerNew{" + neighborhood + "}"; }
}
