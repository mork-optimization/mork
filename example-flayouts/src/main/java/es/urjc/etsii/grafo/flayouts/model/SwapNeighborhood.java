package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Optional;

public class SwapNeighborhood extends RandomizableNeighborhood<FLPMove, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<FLPMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        // Assume first row has at least two elements
        if(solution.allFacilitiesSize() <= 2){
            return ExploreResult.empty();
        }
        return ExploreResult.fromStream(new SwapMove(solution, 0, 1));
    }

    @Override
    public Optional<FLPMove> getRandomMove(FLPSolution solution) {
        int nFacilities = solution.allFacilitiesSize();
        var r = RandomManager.getRandom();
        if(nFacilities <= 2){
            return Optional.empty();
        }

        int origin = r.nextInt(nFacilities);
        int destination = r.nextInt(nFacilities);
        if(origin == destination){
            return getRandomMove(solution);
        }
        return Optional.of(new SwapMove(solution, origin, destination));
    }

    public static class SwapMove extends FLPMove {
        public SwapMove(FLPSolution s, int index1, int index2) {
            super(s, index1, index2, swapCost(s, index1, index2));
        }

        private static double swapCost(FLPSolution solution, int position1, int position2) {

            var ri1 = solution.getRowIndexForPosition(position1);
            var ri2 = solution.getRowIndexForPosition(position2);

            // Antes de hacer el movimiento
            assert solution.equalsSolutionData(solution.calculateCenters());
            double before = solution.getScore();
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            // Do movement
            var temp = solution.rows[ri1.row][ri1.index];
            solution.rows[ri1.row][ri1.index] = solution.rows[ri2.row][ri2.index];
            solution.rows[ri2.row][ri2.index] = temp;

            // Despues de hacer el movimiento
            // todo: if row1 == row2 skip second call
            solution.recalculateCentersInPlace(ri1.row);
            solution.recalculateCentersInPlace(ri2.row);

            double after = solution.recalculateScore();

            // Undo movement
            temp = solution.rows[ri1.row][ri1.index];
            solution.rows[ri1.row][ri1.index] = solution.rows[ri2.row][ri2.index];
            solution.rows[ri2.row][ri2.index] = temp;

            // todo: if row1 == row2 skip second call
            solution.recalculateCentersInPlace(ri1.row);
            solution.recalculateCentersInPlace(ri2.row);

            // Al deshacer el coste deberia quedar igual
            assert DoubleComparator.equals(before, solution.recalculateScore());
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            return after - before;
        }

        @Override
        protected void _execute() {
            this.swap(this.index1, this.index2, this.delta);
        }

        @Override
        public SwapMove next() {
            // Copy for new movement
            int _index1 = index1, _index2 = index2;
            _index2++;
            if (_index2 >= s.allFacilitiesSize()) {
                // Advance _index1 and reset _index2
                _index1++;
                _index2 = _index1 + 1;
                if (_index2 >= s.allFacilitiesSize()) {
                    return null; // End of stream
                }
            }

            return new SwapMove(this.s, _index1, _index2);
        }

        private void swap(int position1, int position2, double cost) {
            var ri1 = this.s.getRowIndexForPosition(position1);
            var ri2 = this.s.getRowIndexForPosition(position2);

            var solution = getSolution();
            solution.cachedScore += cost;
            var temp = solution.rows[ri1.row][ri1.index];
            solution.rows[ri1.row][ri1.index] = solution.rows[ri2.row][ri2.index];
            solution.rows[ri2.row][ri2.index] = temp;

            // todo: if row1 == row2 skip second call
            solution.recalculateCentersInPlace(ri1.row);
            solution.recalculateCentersInPlace(ri2.row);
            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }
    }
}

