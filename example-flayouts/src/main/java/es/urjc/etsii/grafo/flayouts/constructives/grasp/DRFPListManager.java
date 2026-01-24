package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.drflp.model.FLPInstance;
import es.urjc.etsii.grafo.drflp.model.FLPSolution;
import es.urjc.etsii.grafo.solver.create.grasp.GRASPListManager;

import java.util.ArrayList;
import java.util.List;

public class DRFPListManager extends GRASPListManager<DRFPAddMove, FLPSolution, FLPInstance> {

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     * @param solution Current solution
     * @return a candidate list
     */
    @Override
    public List<DRFPAddMove> buildInitialCandidateList(FLPSolution solution) {
        var list = new ArrayList<DRFPAddMove>();
        int nRows = solution.getInstance().getNRows();

        // Generate a list with all valid movements for current solution
        for(var f: solution.getNotAssignedFacilities()){
            for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
                // Iterate backwards so it matches InsertBySwap order
                //for (int j = 0; j <= solution.getRowSize(rowIndex); j++) {
                for (int j = solution.getRowSize(rowIndex); j >= 0; j--) {
                    var move = new DRFPAddMove(solution, rowIndex, j, f);
                    list.add(move);
                }
            }
        }

        return list;
    }

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param solution Current solution, move has been already applied
     * @param move     Chosen move
     * @param index index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    @Override
    public List<DRFPAddMove> updateCandidateList(FLPSolution solution, DRFPAddMove move, List<DRFPAddMove> candidateList, int index) {
        // List can be partially updated / modified
        // recalculating from scratch is an ok start and can be optimized latter if necessary
        return buildInitialCandidateList(solution);
    }

}
