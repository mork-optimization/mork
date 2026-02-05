//package es.urjc.etsii.grafo.flayouts.model;
//
//import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
//import es.urjc.etsii.grafo.util.ArrayUtil;
//import es.urjc.etsii.grafo.util.DoubleComparator;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//public class MoveBySwapNeigh extends Neighborhood<MoveBySwapNeigh.MoveBySwap, FLPSolution, FLPInstance> {
//
//    @Override
//    public List<MoveBySwap> getMovements(FLPSolution solution) {
//        var moves = new ArrayList<MoveBySwap>();
//        var clone = new FLPSolution(solution);
//        assert DoubleComparator.equals(clone.getScore(), solution.getScore());
//        double initialScore = solution.getScore();
//
//        for (int i = 0; i < solution.rows.length; i++) {
//            generateRowMoves(moves, solution, i);
//        }
//
//        assert DoubleComparator.equals(initialScore, solution.recalculateScore());
//        assert DoubleComparator.equals(clone.getScore(), solution.getScore());
//        return moves;
//    }
//
//    protected void generateRowMoves(List<MoveBySwap> moves, FLPSolution solution, int rowIndex) {
//        var rowData = solution.getRows()[rowIndex];
//        int rowSize = solution.rowSize(rowIndex);
//
//        for (int position = 0; position < solution.rowSize[rowIndex]; position++) {
//            var target = rowData[position];
//
//            // Moves available in current row
//            right2LeftForPosition(moves, solution, rowIndex, position);
//            left2RightForPosition(moves, solution, rowIndex, position);
//
//
//            // Moves available in other rows
//            for (int insertRow = 0; insertRow < solution.rows.length; insertRow++) {
//                if(rowIndex == insertRow){
//                    continue;
//                }
//                // Prepare using existing move
//                int insertIndex = solution.rowSize(insertRow);
//                var insertRI = new FLPSolution.RowIndex(insertRow, insertIndex);
//                var originRI = new FLPSolution.RowIndex(rowIndex, position);
//
//                var prepareMove = new HelperMove(solution, originRI, insertRI);
//                double baseCost = prepareMove.getValue();
//                prepareMove._execute();
//
//                assert solution.rowSize(rowIndex) == rowSize -1;
//
//                // Do
//                right2LeftForPosition(baseCost, rowIndex, position, moves, solution, insertRow, insertIndex);
//
//                // Undo using existing move
//                var undoMove = new HelperMove(solution, insertRI, originRI);
//                assert DoubleComparator.equals(-baseCost, undoMove.getValue());
//                undoMove._execute();
//                assert solution.rowSize(rowIndex) == rowSize;
//            }
//
//            // Undo prepare for multirow movebyswap
////            ArrayUtils.deleteAndInsert(rowData, rowSize - 1, position);
////            solution.recalculateCentersInPlace(rowIndex);
//        }
//    }
//
//
//    public static void left2RightForPosition(List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
//        // El score debe ser el mismo antes y despues
//        int rowSize = solution.rowSize[row];
//        double initialScore = solution.getScore();
//        double accCost = 0;
//        for (int j = position; j < rowSize - 1; j++) {
//            double _cost = consecutiveSwapCost(solution, row, j, j + 1);
//            accCost += _cost;
//            var move = new MoveBySwap(solution, row, row, position, j, accCost, true);
//            moves.add(move);
//        }
//        // Undo insert by swap
//        // [a,b,c,d,e] ends like [b,c,d,e,a], delete last and insert on first position.
//        ArrayUtil.deleteAndInsert(solution.rows[row], solution.rowSize[row] - 1, position);
//        solution.updateCenters(row);
//
//        double finalScore = solution.getScore();
//        assert DoubleComparator.equals(initialScore, finalScore);
//        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
//    }
//
//    public static void right2LeftForPosition(List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
//        // El score debe ser el mismo antes y despues
//        double initialScore = solution.getScore();
//        double accCost = 0;
//        for (int j = position; j > 0; j--) {
//            //double _cost = consecutiveSwapCost(solution, getAccCostMatrix(solution), row, j - 1, j);
//            double _cost = consecutiveSwapCost(solution, row, j - 1, j);
//            //assert DoubleComparator.equals(_cost, consecutiveSwapCostNOT_OPTIMIZED(solution, row, j, j + 1));
//            accCost += _cost;
//            var move = new MoveBySwap(solution, row, row, position, j, accCost, false);
//            moves.add(move);
//        }
//
//        // Undo insert by swap
//        // [a,b,c,d,e] ends like [e,a,b,c,d], delete last and insert on first position.
//        ArrayUtil.deleteAndInsert(solution.rows[row], 0, position);
//        solution.updateCenters(row);
//
//        double finalScore = solution.getScore();
//        assert DoubleComparator.equals(initialScore, finalScore);
//        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
//    }
//
//    public static void right2LeftForPosition(double baseCost, int originalRow, int originalIndex, List<MoveBySwap> moves, FLPSolution solution, int row, int position) {
//        // El score debe ser el mismo antes y despues
//        double initialScore = solution.getScore();
//        double accCost = baseCost;
//        for (int j = position; j > 0; j--) {
//            //double _cost = consecutiveSwapCost(solution, getAccCostMatrix(solution), row, j - 1, j);
//            double _cost = consecutiveSwapCost(solution, row, j - 1, j);
//            //assert DoubleComparator.equals(_cost, consecutiveSwapCostNOT_OPTIMIZED(solution, row, j, j + 1));
//            accCost += _cost;
//            var move = new MoveBySwap(solution, originalRow, row, originalIndex, j, accCost, false);
//            moves.add(move);
//        }
//
//        // Undo insert by swap
//        // [a,b,c,d,e] ends like [e,a,b,c,d], delete last and insert on first position.
//        ArrayUtil.deleteAndInsert(solution.rows[row], 0, position);
//        solution.updateCenters(row);
//
//        double finalScore = solution.getScore();
//        assert DoubleComparator.equals(initialScore, finalScore);
//        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
//    }
//
//
//
//
//
//    public static class MoveBySwap extends FLPMove<FLPSolution, FLPInstance> {
//
//        final int rowOrigin, rowDest, indexOrig, indexDest;
//        final double scoreChange;
//        final boolean toRight;
//
//        public MoveBySwap(FLPSolution FLPSolution, int rowOrigin, int rowDest, int indexOrig, int indexDest, double scoreChange, boolean toRight) {
//            super(FLPSolution);
//            this.rowOrigin = rowOrigin;
//            this.rowDest = rowDest;
//            this.indexOrig = indexOrig;
//            this.indexDest = indexDest;
//            this.scoreChange = scoreChange;
//            this.toRight = toRight;
//        }
//
//        @Override
//        protected FLPSolution _execute(FLPSolution solution) {
//            int index2 = this.toRight ? this.indexDest + 1 : this.indexDest - 1;
//            MoveBySwap.move(solution, this.rowOrigin, this.indexOrig, this.rowDest, index2, this.scoreChange);
//            return solution;
//        }
//
//        private static void move(FLPSolution solution, int row1, int index1, int row2, int index2, double score){
//
//            if(row1 == row2){
//                solution.cachedScore += score;
//                ArrayUtil.deleteAndInsert(solution.rows[row1], index1, index2);
//                solution.updateCenters(row1);
//            } else {
//                var value = ArrayUtil.remove(solution.rows[row1], index1);
//                ArrayUtil.insert(solution.rows[row2], index2, value);
//                solution.rowSize[row1]--;
//                solution.rows[row1][solution.rowSize[row1]] = FLPSolution.FREE_SPACE;
//                solution.rowSize[row2]++;
//                solution.cachedScore += score;
//                solution.updateCenters(row1);
//                solution.updateCenters(row2);
//            }
//
//            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("(%s, %s) => (%s, %s); c=%s", rowOrigin, indexOrig, rowDest, indexDest, this.scoreChange);
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            MoveBySwap that = (MoveBySwap) o;
//            return rowOrigin == that.rowOrigin && rowDest == that.rowDest && indexOrig == that.indexOrig && indexDest == that.indexDest && Double.compare(that.scoreChange, scoreChange) == 0 && toRight == that.toRight;
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(rowOrigin, rowDest, indexOrig, indexDest, scoreChange, toRight);
//        }
//
//
//        public int getRowDest() {
//            return this.rowDest;
//        }
//
//        public int getIndexDest() {
//            return this.toRight ? this.indexDest + 1 : this.indexDest - 1;
//        }
//
//        public double getScoreChange() {
//            return scoreChange;
//        }
//    }
//
//    protected static class HelperMove extends FLPMove<FLPSolution, FLPInstance> {
//        private final double score;
//        private final FLPSolution.RowIndex ri1, ri2;
//
//        public HelperMove(FLPSolution s, FLPSolution.RowIndex ri1, FLPSolution.RowIndex ri2) {
//            super(s);
//            this.ri1 = ri1;
//            this.ri2 = ri2;
//            this.score = moveCost(s, ri1, ri2);
//        }
//
//
//        public static double moveCost(FLPSolution solution, FLPSolution.RowIndex ri1, FLPSolution.RowIndex ri2) {
//
//            // Antes de hacer el movement
//            assert solution.equalsSolutionData(solution.calculateCenters());
//            double before = solution.getScore();
//            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
//
//            double after;
//
//            if(ri1.row == ri2.row){
//                // Do movement
//                ArrayUtil.deleteAndInsert(solution.rows[ri1.row], ri1.index, ri2.index);
//
//                // Despues de hacer el movimiento
//                after = solution.recalculateScore();
//
//                // Undo movement
//                ArrayUtil.deleteAndInsert(solution.rows[ri1.row], ri2.index, ri1.index);
//
//                // Al deshacer el coste deberia quedar igual
//                solution.updateCenters(ri1.row);
//
//            } else {
//                var value = ArrayUtil.remove(solution.rows[ri1.row], ri1.index);
//                ArrayUtil.insert(solution.rows[ri2.row], ri2.index, value);
//                solution.rowSize[ri1.row]--;
//                solution.rowSize[ri2.row]++;
//                after = solution.recalculateScore();
//
//                value = ArrayUtil.remove(solution.rows[ri2.row], ri2.index);
//                ArrayUtil.insert(solution.rows[ri1.row], ri1.index, value);
//                solution.rowSize[ri1.row]++;
//                solution.rowSize[ri2.row]--;
//            }
//
//            assert DoubleComparator.equals(before, solution.recalculateScore());
//            return after - before;
//        }
//
//        @Override
//        protected FLPSolution _execute(FLPSolution solution) {
//
//            if(ri1.row == ri2.row){
//                solution.cachedScore += score;
//                ArrayUtil.deleteAndInsert(solution.rows[ri1.row], ri1.index, ri2.index);
//                solution.updateCenters(ri1.row);
//            } else {
//                var value = ArrayUtil.remove(solution.rows[ri1.row], ri1.index);
//                ArrayUtil.insert(solution.rows[ri2.row], ri2.index, value);
//                solution.rowSize[ri1.row]--;
//                solution.rowSize[ri2.row]++;
//                solution.cachedScore += this.score;
//                solution.updateCenters(ri1.row);
//                solution.updateCenters(ri2.row);
//            }
//
//            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
//        }
//
//        @Override
//        public String toString() {
//            return "RelocateMove{" +
//                    "score=" + score +
//                    ", ri1=" + ri1 +
//                    ", ri2=" + ri2 +
//                    '}';
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            HelperMove moveMove = (HelperMove) o;
//            return Double.compare(moveMove.score, score) == 0 && Objects.equals(ri1, moveMove.ri1) && Objects.equals(ri2, moveMove.ri2);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(score, ri1, ri2);
//        }
//
//    }
//}
