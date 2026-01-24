package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Move one element A from its position I to another position J, displacing all elements between those positions
 */
public class MoveNeighborhood extends RandomizableNeighborhood<FLPMove, FLPSolution, FLPInstance> {

    @Override
    public Stream<FLPMove> stream(FLPSolution solution) {
        if(solution.allFacilitiesSize() <= 2){
            return Stream.empty();
        }
        return buildStream(new MoveMove(solution,0, 1));
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
        return Optional.of(new MoveMove(solution, origin, destination));
    }

    protected static class MoveMove extends FLPMove {
        public MoveMove(FLPSolution solution, int index1, int index2) {
            super(solution, index1, index2, moveCost(solution, index1, index2));
        }

        public static double moveCost(FLPSolution solution, int position1, int position2) {
            var ri1 = solution.getRowIndexForPosition(position1);
            var ri2 = solution.getRowIndexForPosition(position2);

            // Antes de hacer el movement
            assert solution.equalsSolutionData(solution.recalculateCentersCopy());
            double before = solution.getScore();
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            double after;

            if(ri1.row == ri2.row){
                // Do movement
                ArrayUtil.deleteAndInsert(solution.solutionData[ri1.row], ri1.index, ri2.index);

                // Despues de hacer el movimiento
                after = solution.recalculateScore();

                // Undo movement
                ArrayUtil.deleteAndInsert(solution.solutionData[ri1.row], ri2.index, ri1.index);

                // Al deshacer el coste deberia quedar igual
                solution.recalculateCentersInPlace(ri1.row);

            } else {
                var value = ArrayUtil.remove(solution.solutionData[ri1.row], ri1.index);
                ArrayUtil.insert(solution.solutionData[ri2.row], ri2.index, value);
                solution.rowSize[ri1.row]--;
                solution.solutionData[ri1.row][solution.rowSize[ri1.row]] = null;
                solution.rowSize[ri2.row]++;
                after = solution.recalculateScore();

                value = ArrayUtil.remove(solution.solutionData[ri2.row], ri2.index);
                ArrayUtil.insert(solution.solutionData[ri1.row], ri1.index, value);
                solution.rowSize[ri1.row]++;
                solution.rowSize[ri2.row]--;
                solution.solutionData[ri2.row][solution.rowSize[ri2.row]] = null;
            }

            assert DoubleComparator.equals(before, solution.recalculateScore());
            return after - before;
        }

        @Override
        protected void _execute() {
            this.move(this.index1, this.index2, this.score);
        }

        @Override
        public MoveMove next() {
            // Copy for new movement
            int _index1 = index1, _index2 = index2;
            _index2++;
            if (_index2 >= s.allFacilitiesSize()) {
                // Advance _index1 and reset _index2
                _index1++;
                _index2 = 0;
                if (_index1 >= s.allFacilitiesSize()) {
                    return null; // End of stream
                }
            }

            var m = new MoveMove(this.s, _index1, _index2);
            return _index1 == _index2 ? m.next() : m;
        }


        public void move(int position1, int position2, double cost) {
            var solution = getSolution();

            var ri1 = solution.getRowIndexForPosition(position1);
            var ri2 = solution.getRowIndexForPosition(position2);

            if(ri1.row == ri2.row){
                solution.cachedScore += cost;
                ArrayUtil.deleteAndInsert(solution.solutionData[ri1.row], ri1.index, ri2.index);
                solution.recalculateCentersInPlace(ri1.row);
            } else {
                var value = ArrayUtil.remove(solution.solutionData[ri1.row], ri1.index);
                ArrayUtil.insert(solution.solutionData[ri2.row], ri2.index, value);
                solution.rowSize[ri1.row]--;
                solution.solutionData[ri1.row][solution.rowSize[ri1.row]] = null;

                solution.rowSize[ri2.row]++;
                solution.cachedScore += this.score;
                solution.recalculateCentersInPlace(ri1.row);
                solution.recalculateCentersInPlace(ri2.row);
            }

            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }

        @Override
        public String toString() {
            var rowindex1 = s.getRowIndexForPosition(this.index1);
            var rowindex2 = s.getRowIndexForPosition(this.index2);
            return String.format("(%s, %s) => (%s, %s); c=%s", rowindex1.row, rowindex1.index, rowindex2.row, rowindex2.index, this.score);
        }
    }
}

