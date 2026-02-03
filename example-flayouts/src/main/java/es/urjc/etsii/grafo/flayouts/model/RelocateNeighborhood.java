package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Move one element A from its position I to another position J, displacing all elements between those positions
 */
public class RelocateNeighborhood extends RandomizableNeighborhood<RelocateNeighborhood.RelocateMove, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<RelocateNeighborhood.RelocateMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        if(solution.nAssigned() <= 2){
            return ExploreResult.empty();
        }
        List<RelocateMove> moves = new ArrayList<>();

        int nRows = solution.nRows();
        // Generate each unordered pair once:
        // - (A,B) == (B,A) -> only keep lexicographically increasing (row,pos) pairs
        // - (A,A) is useless -> excluded by starting pos2 after pos1 when row2 == row1
        for (int row1 = 0; row1 < nRows; row1++) {
            for (int pos1 = 0; pos1 < solution.rowSize[row1]; pos1++) {
                for (int row2 = row1; row2 < nRows; row2++) {
                    int startPos2 = (row2 == row1) ? (pos1 + 1) : 0;

                    for (int pos2 = startPos2; pos2 < solution.rowSize[row2]; pos2++) {
                        moves.add(new RelocateMove(solution, row1, pos1, row2, pos2));
                    }
                }
            }
        }

        return ExploreResult.fromList(moves);
    }

    @Override
    public Optional<RelocateNeighborhood.RelocateMove> getRandomMove(FLPSolution solution) {
        int nAssigned = solution.nAssigned();
        if(nAssigned <= 2){
            return Optional.empty();
        }

        var r = RandomManager.getRandom();

        int a = r.nextInt(nAssigned-1), b = r.nextInt(nAssigned);

        if (a == b) b++; // guaranteed does not overflow because if a == b, then b is at least < nAssigned -1.

        // Make (A,B) equivalent to (B,A)
        int origin = Math.min(a, b);
        int destination = Math.max(a, b);

        int row1 = -1, pos1 = -1, row2 = -1, pos2 = -1;
        int idx = 0;
        for (int row = 0; row < solution.nRows(); row++) {
            // find correct row and position offsets for both origin and destination
            int size = solution.rowSize[row];
            if (origin >= idx && origin < idx + size) {
                row1 = row;
                pos1 = origin - idx;
            }
            if (destination >= idx && destination < idx + size) {
                row2 = row;
                pos2 = destination - idx;
            }
            idx += size;

            if (row1 != -1 && row2 != -1) break; // already found both
        }

        assert row1 != -1 && row2 != -1 && pos1 != -1 && pos2 != -1;
        return Optional.of(new RelocateMove(solution, row1, pos1, row2, pos2));
    }

    public static class RelocateMove extends FLPMove {
        private final int row1, pos1, row2, pos2;

        public RelocateMove(FLPSolution solution, int row1, int pos1, int row2, int pos2) {
            this(solution, row1, pos1, row2, pos2, moveCost(solution, row1, pos1, row2, pos2));
        }

        public RelocateMove(FLPSolution solution, int row1, int pos1, int row2, int pos2, double cost) {
            super(solution, cost);
            this.row1 = row1;
            this.pos1 = pos1;
            this.row2 = row2;
            this.pos2 = pos2;
        }

        public static double moveCost(FLPSolution solution, int row1, int pos1, int row2, int pos2) {

            // Antes de hacer el movement
            double before = solution.getScore();
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            double after;

            if(row1 == row2){
                // Do movement
                ArrayUtil.deleteAndInsert(solution.rows[row1], pos1, pos2);

                // Despues de hacer el movimiento
                after = solution.recalculateScore();

                // Undo movement
                ArrayUtil.deleteAndInsert(solution.rows[row1], pos2, pos1);

                // Al deshacer el coste deberia quedar igual
                solution.updateCenters(row1);

            } else {
                var value = ArrayUtil.remove(solution.rows[row1], pos1);
                ArrayUtil.insert(solution.rows[row2], pos2, value);
                solution.rowSize[row1]--;
                solution.rows[row1][solution.rowSize[row1]] = FLPSolution.FREE_SPACE;
                solution.rowSize[row2]++;
                after = solution.recalculateScore();

                value = ArrayUtil.remove(solution.rows[row2], pos2);
                ArrayUtil.insert(solution.rows[row1], pos1, value);
                solution.rowSize[row1]++;
                solution.rowSize[row2]--;
                solution.rows[row2][solution.rowSize[row2]] = FLPSolution.FREE_SPACE;
            }

            assert DoubleComparator.equals(before, solution.recalculateScore());
            return after - before;
        }


        @Override
        protected FLPSolution _execute(FLPSolution solution) {
            if(row1 == row2){
                solution.cachedScore += delta;
                ArrayUtil.deleteAndInsert(solution.rows[row1], pos1, pos2);
                solution.updateCentersFromTo(row1, pos1, pos2+1);
            } else {
                var value = ArrayUtil.remove(solution.rows[row1], pos1);
                ArrayUtil.insert(solution.rows[row2], pos2, value);
                solution.rowSize[row1]--;
                solution.rows[row1][solution.rowSize[row1]] = FLPSolution.FREE_SPACE;

                solution.rowSize[row2]++;
                solution.cachedScore += this.delta;
                solution.updateCenters(row1);
                solution.updateCenters(row2);
            }

            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
            return solution;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s) => (%s, %s); c=%s", row1, pos1, row2, pos2, this.delta);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RelocateMove that = (RelocateMove) o;
            return row1 == that.row1 && pos1 == that.pos1 && row2 == that.row2 && pos2 == that.pos2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row1, pos1, row2, pos2);
        }
    }
}

