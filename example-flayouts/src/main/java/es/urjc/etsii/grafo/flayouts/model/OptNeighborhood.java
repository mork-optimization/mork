package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.stream.Stream;

public class OptNeighborhood extends Neighborhood<FLPMove, FLPSolution, FLPInstance> {

    @Override
    public Stream<FLPMove> stream(FLPSolution solution) {
        // Assume first row has at least two elements
        Stream<FLPMove> stream = Stream.empty();
        for (int i = 0; i < solution.nRows(); i++) {
            if (solution.rowSize(i) > 1) {
                stream = Stream.concat(stream, buildStream(new OptMove(solution, i, 0, 1)));
            }
        }
        return stream;
    }

    protected static class OptMove extends FLPMove {

        private final int row;

        public OptMove(FLPSolution s, int row, int index1, int index2) {
            super(s, index1, index2, twoOptCost(s, row, index1, index2));
            this.row = row;
        }

        // Neighborhoods cost calculation and move execution
        private static double twoOptCost(FLPSolution solution, int row, int index1, int index2) {
            // Antes de hacer el movimiento
            assert solution.equalsSolutionData(solution.calculateCenters());
            double before = solution.partialCost(row, index1, index2);

            // Do movement
            ArrayUtil.reverseFragment(solution.rows[row], index1, index2);

            // Despues de hacer el movimiento
            solution.recalculateCentersInPlace(row);
            double after = solution.partialCost(row, index1, index2);

            // Undo movement
            ArrayUtil.reverseFragment(solution.rows[row], index1, index2);

            // Al deshacer el coste deberia quedar igual
            solution.recalculateCentersInPlace(row);
            assert DoubleComparator.equals(before, solution.partialCost(row, index1, index2));

            return after - before;
        }

        @Override
        protected void _execute() {
            this.twoOpt(this.row, this.index1, this.index2, this.delta);
        }

        @Override
        public OptMove next() {
            // Copy for new movement
            int _row = row, _index1 = index1, _index2 = index2;
            _index2++;
            if (_index2 >= s.getRowSize(_row)) {
                // Advance _index1 and reset _index2
                _index1++;
                _index2 = _index1 + 1;
                if (_index2 >= s.getRowSize(_row)) {
                    return null; // End of stream
                }
            }

            return new OptMove(this.s, _row, _index1, _index2);
        }

        public void twoOpt(int row, int index1, int index2, double cost) {
            var solution = getSolution();
            solution.cachedScore += cost;
            ArrayUtil.reverseFragment(solution.solutionData[row], index1, index2);
            solution.recalculateCentersInPlace(row);
            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }
    }
}

