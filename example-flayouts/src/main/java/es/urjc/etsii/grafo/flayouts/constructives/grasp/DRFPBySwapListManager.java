package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.drflp.model.FLPInstance;
import es.urjc.etsii.grafo.drflp.model.FLPSolution;
import es.urjc.etsii.grafo.drflp.model.Facility;
import es.urjc.etsii.grafo.drflp.model.MoveBySwapNeighborhood;
import es.urjc.etsii.grafo.solver.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

public class DRFPBySwapListManager extends GRASPListManager<DRFPAddMove, FLPSolution, FLPInstance> {

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     *
     * @param solution Current solution
     * @return a candidate list
     */
    @Override
    public List<DRFPAddMove> buildInitialCandidateList(FLPSolution solution) {
        var list = new ArrayList<DRFPAddMove>();
        int nRows = solution.getInstance().getNRows();

        var pendingFacilities = new ArrayList<>(solution.getNotAssignedFacilities());
        for (var f : pendingFacilities) {
            for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
                if (solution.getRowSize(rowIndex) > 0) {
                    doInsertBySwap(list, solution, rowIndex, f);
                } else {
                    list.add(new DRFPAddMove(solution, rowIndex, 0, f));
                }
            }
        }

        return list;
    }

    private void doInsertBySwap(List<DRFPAddMove> moves, FLPSolution solution, int rowIndex, Facility f) {
        var prevScore = solution.getScore();
        ValidationUtil.assertValidScore(solution);

        // Put facility at end of row
        int index = solution.getRowSize(rowIndex);
        double accCost = insertAtLast(solution, rowIndex, f);
        moves.add(new DRFPAddMove(solution, rowIndex, index, f, accCost));

        // Do insert by swap, facility ends at 0 index
        var _moves = new ArrayList<MoveBySwapNeighborhood.MoveBySwap>();
        MoveBySwapNeighborhood.right2LeftConstructive(accCost, _moves, solution, rowIndex, index);

        for (var _move : _moves) {
            var move = new DRFPAddMove(solution, _move.getRowDest(), _move.getIndexDest(), f, _move.getScoreChange());
            moves.add(move);
        }
        // Delete facility from solution
        deleteFirst(solution, rowIndex, f);

        ValidationUtil.assertValidScore(solution);
        assert DoubleComparator.equals(solution.getScore(), prevScore);

    }

    private void deleteFirst(FLPSolution solution, int rowId, Facility f) {
        var rowData = solution.getSolutionData()[rowId];
        // Verify 0 facility is our target
        assert rowData[0].facility.id == f.id;
        solution.remove(rowId, 0);
    }

    private double insertAtLast(FLPSolution solution, int rowId, Facility f) {
        int position = solution.getRowSize(rowId);
        DRFPAddMove addMove = new DRFPAddMove(solution, rowId, position, f);
        // Bypass framework checks, do not increment s.version
        double oldScore = solution.getScore();
        double delta = addMove.getValue();
        addMove._execute();

        assert DoubleComparator.equals(oldScore + delta, solution.getScore());
        ValidationUtil.assertValidScore(solution);

        return delta;
    }

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param solution      Current solution, move has been already applied
     * @param move          Chosen move
     * @param index         index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    @Override
    public List<DRFPAddMove> updateCandidateList(FLPSolution solution, DRFPAddMove move, List<DRFPAddMove> candidateList, int index) {
        // List can be partially updated / modified
        // recalculating from scratch is ok, can be optimized latter if necessary
        return buildInitialCandidateList(solution);
    }

}
