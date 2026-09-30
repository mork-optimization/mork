package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Insert any missing facility at any row gap without changing the source during evaluation. */
public class FLPAddNeigh extends RandomizableNeighborhood<FLPAddNeigh.AddMove, FLPSolution, FLPInstance> {
    private final boolean fast;

    @AutoconfigConstructor
    public FLPAddNeigh() { this(true); }

    /** Full-cost, eager evaluation is available only for tests and validation. */
    FLPAddNeigh(boolean fast) { this.fast = fast; }

    private FLPNewUtil.MoveSpace<AddMove> space(FLPSolution solution, List<Integer> facilities) {
        var gaps = FLPNewUtil.gaps(solution);
        return new FLPNewUtil.MoveSpace<>((long) facilities.size() * gaps.length, index -> {
            int facility = facilities.get((int) (index / gaps.length));
            var gap = gaps[(int) (index % gaps.length)];
            double delta = FLPNewUtil.insertionDelta(solution, facility, gap.row(), gap.pos(), fast);
            return new AddMove(solution, gap.row(), gap.pos(), facility, delta);
        });
    }

    @Override
    public ExploreResult<AddMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        if (TimeControl.isTimeUp()) return ExploreResult.empty();
        return FLPNewUtil.explore(space(solution, FLPNewUtil.missing(solution)), fast);
    }

    public List<AddMove> exploreList(FLPSolution solution) {
        try (var moves = explore(solution).moves()) { return moves.toList(); }
    }

    public void exploreForFacility(List<AddMove> list, FLPSolution solution, int facility) {
        if (TimeControl.isTimeUp()) return;
        try (var moves = FLPNewUtil.explore(space(solution, List.of(facility)), fast).moves()) {
            list.addAll(moves.toList());
        }
    }

    @Override
    public Optional<AddMove> getRandomMove(FLPSolution solution) {
        if (TimeControl.isTimeUp()) return Optional.empty();
        return FLPNewUtil.randomMove(space(solution, FLPNewUtil.missing(solution)));
    }

    @Override
    public int neighborhoodSize(FLPSolution solution) {
        long size = (long) solution.getNotAssignedFacilities().size() * (solution.nAssigned() + solution.nRows());
        return (int) Math.min(Integer.MAX_VALUE - 1L, size);
    }

    public static double insertCost(FLPSolution solution, int row, int pos, int facility) {
        return FLPNewUtil.insertionDelta(solution, facility, row, pos, true);
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
