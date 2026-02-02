package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoveBySwapNeighborhood extends Neighborhood<MoveBySwapNeighborhood.MoveBySwap, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<MoveBySwap, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        return null;
    }

    @Override
    public List<MoveBySwap> getMovements(FLPSolution solution) {
        var moves = new ArrayList<MoveBySwap>();
        var clone = new FLPSolution(solution);
        assert DoubleComparator.equals(clone.getScore(), solution.getScore());
        assert clone.equalsSolutionData(solution.getRows());
        double initialScore = solution.getScore();

        for (int i = 0; i < solution.rows.length; i++) {
            generateRowMoves(moves, solution, i);
        }

        assert DoubleComparator.equals(initialScore, solution.recalculateScore());
        assert DoubleComparator.equals(clone.getScore(), solution.getScore());
        assert clone.equalsSolutionData(solution.getRows());
        return moves;
    }

    protected void generateRowMoves(List<MoveBySwap> moves, FLPSolution solution, int rowIndex) {
        var rowData = solution.getRows()[rowIndex];
        int rowSize = solution.rowSize(rowIndex);

        for (int position = 0; position < solution.rowSize[rowIndex]; position++) {
            var target = rowData[position];

            // Moves available in current row
            right2LeftForPosition(moves, solution, rowIndex, position);
            left2RightForPosition(moves, solution, rowIndex, position);

            // Prepare for multirow movebyswap
//            ArrayUtils.deleteAndInsert(rowData, position, rowSize-1);
//            solution.recalculateCentersInPlace(rowIndex);
//            assert target == rowData[rowSize-1];

            // Moves available in other rows
            for (int insertRow = 0; insertRow < solution.rows.length; insertRow++) {
                if(rowIndex == insertRow){
                    continue;
                }
                // Prepare using existing move
                int insertIndex = solution.rowSize(insertRow);
                var insertRI = new FLPSolution.RowIndex(insertRow, insertIndex);
                var originRI = new FLPSolution.RowIndex(rowIndex, position);

                var prepareMove = new HelperMove(solution, originRI, insertRI);
                double baseCost = prepareMove.getValue();
                prepareMove._execute();

                assert solution.rowSize(rowIndex) == rowSize -1;

                // Do
                right2LeftForPosition(baseCost, rowIndex, position, moves, solution, insertRow, insertIndex);

                // Undo using existing move
                var undoMove = new HelperMove(solution, insertRI, originRI);
                assert DoubleComparator.equals(-baseCost, undoMove.getValue());
                undoMove._execute();
                assert solution.rowSize(rowIndex) == rowSize;
            }

            // Undo prepare for multirow movebyswap
//            ArrayUtils.deleteAndInsert(rowData, rowSize - 1, position);
//            solution.recalculateCentersInPlace(rowIndex);
        }
    }


    public static void left2RightForPosition(List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
        // El score debe ser el mismo antes y despues
        int rowSize = solution.rowSize[row];
        double initialScore = solution.getScore();
        double accCost = 0;
        for (int j = position; j < rowSize - 1; j++) {
            double _cost = consecutiveSwapCost(solution, row, j, j + 1);
            accCost += _cost;
            var move = new MoveBySwap(solution, row, row, position, j, accCost, true);
            moves.add(move);
        }
        // Undo insert by swap
        // [a,b,c,d,e] ends like [b,c,d,e,a], delete last and insert on first position.
        ArrayUtil.deleteAndInsert(solution.rows[row], solution.rowSize[row] - 1, position);
        solution.recalculateCentersInPlace(row);

        double finalScore = solution.getScore();
        assert DoubleComparator.equals(initialScore, finalScore);
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
    }

    public static void right2LeftForPosition(List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
        // El score debe ser el mismo antes y despues
        double initialScore = solution.getScore();
        double accCost = 0;
        for (int j = position; j > 0; j--) {
            //double _cost = consecutiveSwapCost(solution, getAccCostMatrix(solution), row, j - 1, j);
            double _cost = consecutiveSwapCost(solution, row, j - 1, j);
            //assert DoubleComparator.equals(_cost, consecutiveSwapCostNOT_OPTIMIZED(solution, row, j, j + 1));
            accCost += _cost;
            var move = new MoveBySwap(solution, row, row, position, j, accCost, false);
            moves.add(move);
        }

        // Undo insert by swap
        // [a,b,c,d,e] ends like [e,a,b,c,d], delete last and insert on first position.
        ArrayUtil.deleteAndInsert(solution.rows[row], 0, position);
        solution.recalculateCentersInPlace(row);

        double finalScore = solution.getScore();
        assert DoubleComparator.equals(initialScore, finalScore);
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
    }

    public static void right2LeftForPosition(double baseCost, int originalRow, int originalIndex, List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
        // El score debe ser el mismo antes y despues
        double initialScore = solution.getScore();
        double accCost = baseCost;
        for (int j = position; j > 0; j--) {
            //double _cost = consecutiveSwapCost(solution, getAccCostMatrix(solution), row, j - 1, j);
            double _cost = consecutiveSwapCost(solution, row, j - 1, j);
            //assert DoubleComparator.equals(_cost, consecutiveSwapCostNOT_OPTIMIZED(solution, row, j, j + 1));
            accCost += _cost;
            var move = new MoveBySwap(solution, originalRow, row, originalIndex, j, accCost, false);
            moves.add(move);
        }

        // Undo insert by swap
        // [a,b,c,d,e] ends like [e,a,b,c,d], delete last and insert on first position.
        ArrayUtil.deleteAndInsert(solution.rows[row], 0, position);
        solution.recalculateCentersInPlace(row);

        double finalScore = solution.getScore();
        assert DoubleComparator.equals(initialScore, finalScore);
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
    }

    public static void right2LeftConstructive(double baseCost, List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
        // El score debe ser el mismo antes y despues
        double initialScore = solution.getScore();
        double accCost = baseCost;
        for (int j = position; j > 0; j--) {
            //double _cost = consecutiveSwapCost(solution, getAccCostMatrix(solution), row, j - 1, j);
            double _cost = consecutiveSwapCost(solution, row, j - 1, j);
            //assert DoubleComparator.equals(_cost, consecutiveSwapCostNOT_OPTIMIZED(solution, row, j, j + 1));
            accCost += _cost;
            var move = new MoveBySwap(solution, -1, row, -1, j, accCost, false);
            moves.add(move);
        }
    }


    private static double consecutiveSwapCost(FLPSolution solution, int row, int leftIndex, int rightIndex) {
        int rowSize = solution.rowSize[row];
        var rowData = solution.rows[row];
        var instance = solution.getInstance();

        assert leftIndex < rightIndex : String.format("Left index (%s) must be strictly smaller than right index (%s)", leftIndex, rightIndex);
        assert rightIndex < rowSize : String.format("Out of bounds, max is %s, given %s", rowSize, rightIndex);
        assert leftIndex >= 0 : String.format("Out of bounds, min is %s, given %s", 0, leftIndex);

        var leftBox = rowData[leftIndex];
        var rightBox = rowData[rightIndex];

        double changedAreaStart = leftBox.lastCenter - leftBox.facility.width / 2.0D;
        double newLeftCenter = changedAreaStart + rightBox.facility.width / 2.0D;
        double newRightCenter = changedAreaStart + rightBox.facility.width + leftBox.facility.width / 2.0D;


        // Distance moved, used absolute values
        double distanceChangeRight2Left = rightBox.lastCenter - newLeftCenter;
        double distanceChangeLeft2Right = newRightCenter - leftBox.lastCenter;
        double costChange = 0;

        /* AREAS EXPLANATION
        0 ----------------> AREA 3  ----------------> RowSize
        0 ----------------> AREA 3  ----------------> RowSize
        0 --> AREA 1 | index | index + 1 | AREA 2 --> RowSize
        0 ----------------> AREA 3  ----------------> RowSize
        0 ----------------> AREA 3  ----------------> RowSize
         */

        // Area 1
        for (int i = 0; i < leftIndex; i++) {
            var otherbox = rowData[i];
            // Left box moves right increases cost
            costChange += distanceChangeLeft2Right * instance.flow(leftBox.facility, otherbox.facility);
            // Right cost moves left decreases cost
            costChange -= distanceChangeRight2Left * instance.flow(rightBox.facility, otherbox.facility);
        }

        // Area 2
        for (int i = rightIndex + 1; i < rowSize; i++) {
            var otherbox = rowData[i];
            // Left box moves right decreases cost
            costChange -= distanceChangeLeft2Right * instance.flow(leftBox.facility, otherbox.facility);
            // Right cost moves left increases cost
            costChange += distanceChangeRight2Left * instance.flow(rightBox.facility, otherbox.facility);
        }

        // Area 3: All rows different from current
        for (int currentRow = 0; currentRow < solution.rows.length; currentRow++) {
            if(currentRow == row) continue; // Current row already calculated by Area 1 and Area 2
            for (int i = 0; i < solution.rowSize[currentRow]; i++) {
                var otherBox = solution.rows[currentRow][i];
                int leftBoxWeight = instance.flow(otherBox.facility, leftBox.facility);
                double beforeLeftCost = Math.abs(otherBox.lastCenter - leftBox.lastCenter);
                double afterLeftCost = Math.abs(otherBox.lastCenter - newRightCenter);

                // Changed
                costChange += (afterLeftCost - beforeLeftCost) * leftBoxWeight;

                int rightBoxWeight = instance.flow(otherBox.facility, rightBox.facility);
                double beforeRightCost = Math.abs(otherBox.lastCenter - rightBox.lastCenter);
                double afterRightCost = Math.abs(otherBox.lastCenter - newLeftCenter);
                costChange += (afterRightCost - beforeRightCost) * rightBoxWeight;
            }
        }

        // Do fake move
        leftBox.lastCenter = newRightCenter;
        rightBox.lastCenter = newLeftCenter;

        // COST IS NOT UPDATED, but dont need to right?
        rowData[leftIndex] = rightBox;
        rowData[rightIndex] = leftBox;

        return costChange;
    }

    public static class MoveBySwap extends FLPMove<FLPSolution, FLPInstance> {

        final int rowOrigin, rowDest, indexOrig, indexDest;
        final double scoreChange;
        final boolean toRight;

        public MoveBySwap(FLPSolution FLPSolution, int rowOrigin, int rowDest, int indexOrig, int indexDest, double scoreChange, boolean toRight) {
            super(FLPSolution);
            this.rowOrigin = rowOrigin;
            this.rowDest = rowDest;
            this.indexOrig = indexOrig;
            this.indexDest = indexDest;
            this.scoreChange = scoreChange;
            this.toRight = toRight;
        }

        @Override
        protected void _execute() {
            int index2 = this.toRight ? this.indexDest + 1 : this.indexDest - 1;
            MoveBySwap.move(getSolution(), this.rowOrigin, this.indexOrig, this.rowDest, index2, this.scoreChange);
        }

        private static void move(FLPSolution solution, int row1, int index1, int row2, int index2, double score){

            if(row1 == row2){
                solution.cachedScore += score;
                ArrayUtil.deleteAndInsert(solution.rows[row1], index1, index2);
                solution.recalculateCentersInPlace(row1);
            } else {
                var value = ArrayUtil.remove(solution.rows[row1], index1);
                ArrayUtil.insert(solution.rows[row2], index2, value);
                solution.rowSize[row1]--;
                solution.rows[row1][solution.rowSize[row1]] = null;
                solution.rowSize[row2]++;
                solution.cachedScore += score;
                solution.recalculateCentersInPlace(row1);
                solution.recalculateCentersInPlace(row2);
            }

            ValidationUtil.assertValidScore(solution);
            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s) => (%s, %s); c=%s", rowOrigin, indexOrig, rowDest, indexDest, this.scoreChange);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MoveBySwap that = (MoveBySwap) o;
            return rowOrigin == that.rowOrigin && rowDest == that.rowDest && indexOrig == that.indexOrig && indexDest == that.indexDest && Double.compare(that.scoreChange, scoreChange) == 0 && toRight == that.toRight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowOrigin, rowDest, indexOrig, indexDest, scoreChange, toRight);
        }


        public int getRowDest() {
            return this.rowDest;
        }

        public int getIndexDest() {
            return this.toRight ? this.indexDest + 1 : this.indexDest - 1;
        }

        public double getScoreChange() {
            return scoreChange;
        }
    }

    protected static class HelperMove extends FLPMove<FLPSolution, FLPInstance> {
        private final double score;
        private final FLPSolution.RowIndex ri1, ri2;

        public HelperMove(FLPSolution s, FLPSolution.RowIndex ri1, FLPSolution.RowIndex ri2) {
            super(s);
            this.ri1 = ri1;
            this.ri2 = ri2;
            this.score = moveCost(s, ri1, ri2);
        }


        public static double moveCost(FLPSolution solution, FLPSolution.RowIndex ri1, FLPSolution.RowIndex ri2) {

            // Antes de hacer el movement
            assert solution.equalsSolutionData(solution.calculateCenters());
            double before = solution.getScore();
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            double after;

            if(ri1.row == ri2.row){
                // Do movement
                ArrayUtil.deleteAndInsert(solution.rows[ri1.row], ri1.index, ri2.index);

                // Despues de hacer el movimiento
                after = solution.recalculateScore();

                // Undo movement
                ArrayUtil.deleteAndInsert(solution.rows[ri1.row], ri2.index, ri1.index);

                // Al deshacer el coste deberia quedar igual
                solution.recalculateCentersInPlace(ri1.row);

            } else {
                var value = ArrayUtil.remove(solution.rows[ri1.row], ri1.index);
                ArrayUtil.insert(solution.rows[ri2.row], ri2.index, value);
                solution.rowSize[ri1.row]--;
                solution.rowSize[ri2.row]++;
                after = solution.recalculateScore();

                value = ArrayUtil.remove(solution.rows[ri2.row], ri2.index);
                ArrayUtil.insert(solution.rows[ri1.row], ri1.index, value);
                solution.rowSize[ri1.row]++;
                solution.rowSize[ri2.row]--;
            }

            assert DoubleComparator.equals(before, solution.recalculateScore());
            return after - before;
        }

        @Override
        protected void _execute() {
            var solution = getSolution();

            if(ri1.row == ri2.row){
                solution.cachedScore += score;
                ArrayUtil.deleteAndInsert(solution.solutionData[ri1.row], ri1.index, ri2.index);
                solution.recalculateCentersInPlace(ri1.row);
            } else {
                var value = ArrayUtil.remove(solution.solutionData[ri1.row], ri1.index);
                ArrayUtil.insert(solution.solutionData[ri2.row], ri2.index, value);
                solution.rowSize[ri1.row]--;
                solution.rowSize[ri2.row]++;
                solution.cachedScore += this.score;
                solution.recalculateCentersInPlace(ri1.row);
                solution.recalculateCentersInPlace(ri2.row);
            }

            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }

        @Override
        public String toString() {
            return "MoveMove{" +
                    "score=" + score +
                    ", ri1=" + ri1 +
                    ", ri2=" + ri2 +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HelperMove moveMove = (HelperMove) o;
            return Double.compare(moveMove.score, score) == 0 && Objects.equals(ri1, moveMove.ri1) && Objects.equals(ri2, moveMove.ri2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(score, ri1, ri2);
        }

    }
}
