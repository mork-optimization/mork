package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.flayouts.model.FLPAddNeigh;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;

import java.util.List;

public class FLPAddListManager extends GRASPListManager<FLPAddNeigh.AddMove, FLPSolution, FLPInstance> {

    private final FLPAddNeigh neigh = new FLPAddNeigh();

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     * @param solution Current solution
     * @return a candidate list     */
    @Override
    public List<FLPAddNeigh.AddMove> buildInitialCandidateList(FLPSolution solution) {
        return neigh.exploreList(solution);
    }

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param solution Current solution, move has been already applied
     * @param move     Chosen move
     * @param index index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list
     */
    @Override
    public List<FLPAddNeigh.AddMove> updateCandidateList(FLPSolution solution, FLPAddNeigh.AddMove move, List<FLPAddNeigh.AddMove> candidateList, int index) {
        return buildInitialCandidateList(solution);
    }

}
