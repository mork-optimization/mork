package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static es.urjc.etsii.grafo.flayouts.model.FLPSolution.FREE_SPACE;
import static es.urjc.etsii.grafo.flayouts.model.FLPSwapNeigh.SwapMove.consecutiveSwapCost;

public class FLPAddNeigh extends Neighborhood<FLPAddNeigh.AddMove, FLPSolution, FLPInstance> {

    private final boolean insertBySwap;

    public FLPAddNeigh() {
        this(true);
    }

    @AutoconfigConstructor
    public FLPAddNeigh(@CategoricalParam(strings = {"true", "false"}) boolean insertBySwap) {
        this.insertBySwap = insertBySwap;
    }

    @Override
    public ExploreResult<AddMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        return ExploreResult.fromList(exploreList(solution));
    }

    public List<AddMove> exploreList(FLPSolution solution){
        var list = new ArrayList<AddMove>();
        for (var facility : solution.notAssignedFacilities) {
            exploreForFacility(list, solution, facility);
        }
        return list;
    }

    public void exploreForFacility(ArrayList<AddMove> list, FLPSolution solution, int facility){
        for (int row = 0; row < solution.nRows(); row++) {
            if (insertBySwap) {
                movesBySwap(list, solution, facility, row);
            } else {
                movesNaive(list, solution, facility, row);
            }
        }
    }

    private void movesNaive(ArrayList<AddMove> list, FLPSolution solution, int facility, int row) {
        int rowSize = solution.rowSize(row);
        // iterate backwards so it matches the generation order of movesBySwap
        for (int pos = rowSize; pos >= 0; pos--) {
            var addMove = new AddMove(solution, row, pos, facility);
            list.add(addMove);
        }
    }

    private void movesBySwap(ArrayList<AddMove> moves, FLPSolution solution, int facility, int row) {
        int rowSize = solution.rowSize(row);
        if (rowSize == 0) {
            moves.add(new AddMove(solution, row, 0, facility));
            return;
        }

        var prevScore = solution.getScore();
        int index = solution.rowSize(row);
        double accCost = insertAtLastCost(solution, rowSize, facility);
        moves.add(new AddMove(solution, index, index, facility, accCost));

        // Do insert by swap, facility ends at 0 index
        var _moves = new ArrayList<AddMove>();
        right2Left(accCost, _moves, solution, row, index, facility);

        // Delete facility from solution
        deleteFirst(solution, row, facility);

        assert DoubleComparator.equals(solution.getScore(), prevScore);
    }

    public static void right2Left(double baseCost, List<AddMove> moves, FLPSolution solution, int row, int initialPos, int facility) {
        // El score debe ser el mismo antes y despues
        double accCost = baseCost;
        for (int pos = initialPos; pos > 0; pos--) {
            double _cost = consecutiveSwapCost(solution, row, pos - 1, pos);
            accCost += _cost;
            var move = new AddMove(solution, row, pos, facility, accCost);
            moves.add(move);
        }
    }


    public static double insertCost(FLPSolution solution, int rowIdx, int pos, int f) {
        int rowSize = solution.rowSize[rowIdx];
        var row = solution.rows[rowIdx];

        // Antes de hacer el movimiento
        double before = solution.partialCost(rowIdx, pos, rowSize - 1);

        // Do movement
        System.arraycopy(row, pos, row, pos + 1, solution.rowSize[rowIdx] - pos);
        row[pos] = f;
        solution.rowSize[rowIdx]++;
        solution.updateCentersFrom(rowIdx, pos);

        // Despues de hacer el movimiento
        double after = solution.partialCost(rowIdx, pos, rowSize);

        // Deshacemos el movimiento
        solution.rowSize[rowIdx]--;
        System.arraycopy(row, pos + 1, row, pos, solution.rowSize[rowIdx] - pos); // Todo, no faltaria un -1 al length?
        row[solution.rowSize[rowIdx]] = FREE_SPACE;
        solution.updateCentersFrom(rowIdx, pos);

        // Al deshacer el coste deberia quedar igual
        assert DoubleComparator.equals(before, solution.partialCost(rowIdx, pos, rowSize - 1));

        return after - before;
    }


    private void deleteFirst(FLPSolution solution, int rowId, int facility) {
        var rowData = solution.getRows()[rowId];
        // Verify 0 facility is our target
        assert rowData[0] == facility;
        solution.remove(rowId, 0);
    }

    private double insertAtLastCost(FLPSolution solution, int row, int facility) {
        int pos = solution.rowSize(row);
        AddMove addMove = new AddMove(solution, row, pos, facility);
        double oldScore = solution.getScore();
        double delta = addMove.delta();
        addMove._execute(solution);

        assert DoubleComparator.equals(oldScore + delta, solution.getScore());

        return delta;
    }

    public static class AddMove extends FLPMove {
        private final int pos;
        private final int rowIdx;
        private final int facility;

        public AddMove(FLPSolution solution, int rowIdx, int pos, int facility, double cost) {
            super(solution, cost);
            this.rowIdx = rowIdx;
            this.pos = pos;
            this.facility = facility;
        }

        public AddMove(FLPSolution solution, int rowIdx, int pos, int facility) {
            this(solution, rowIdx, pos, facility, insertCost(solution, rowIdx, pos, facility));
        }

        public int pos() {
            return pos;
        }

        public int rowIdx() {
            return rowIdx;
        }

        public int facility() {
            return facility;
        }

        @Override
        protected FLPSolution _execute(FLPSolution solution) {
            assert DoubleComparator.equals(solution.cachedScore, solution.recalculateScore());
            assert solution.notAssignedFacilities.contains(facility);
            assert solution.verifyCorrectSizes();

            solution.cachedScore += this.delta;
            var row = solution.rows[rowIdx];

            // Shift elements to the right to make space for the new facility
            System.arraycopy(row, pos, row, pos + 1, solution.rowSize[rowIdx] - pos);
            row[pos] = facility;
            solution.rowSize[rowIdx]++;
            solution.assignedFacilities++;
            solution.notAssignedFacilities.remove(facility);

            // adjust facility centers
            solution.updateCentersFrom(rowIdx, pos);

            assert DoubleComparator.equals(solution.cachedScore, solution.recalculateScore()) : String.format("Score mismatch, expected %s cached is %s", solution.recalculateScore(), solution.cachedScore);
            assert !solution.notAssignedFacilities.contains(facility);
            assert solution.verifyCorrectSizes();
            return solution;
        }

        @Override
        public String toString() {
            return "AddMove{" +
                    "d=" + delta +
                    ", f=" + facility +
                    ", row=" + rowIdx +
                    ", pos=" + pos +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            AddMove addMove = (AddMove) o;
            return pos == addMove.pos && rowIdx == addMove.rowIdx && facility == addMove.facility;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, rowIdx, facility);
        }
    }

}
