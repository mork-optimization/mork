package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class FLPSwapNeigh extends RandomizableNeighborhood<FLPSwapNeigh.SwapMove, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<SwapMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        // Assume first row has at least two elements
        if (solution.nAssigned() <= 2) {
            return ExploreResult.empty();
        }
        var moves = new ArrayList<SwapMove>();

        return ExploreResult.fromList(moves);
    }

    @Override
    public Optional<SwapMove> getRandomMove(FLPSolution solution) {
        int nAssigned = solution.nAssigned();
        if (nAssigned <= 2) {
            return Optional.empty();
        }

        var r = RandomManager.getRandom();

        int a = r.nextInt(nAssigned - 1), b = r.nextInt(nAssigned);

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
        return Optional.of(new SwapMove(solution, row1, pos1, row2, pos2));
    }

    public static class SwapMove extends FLPMove {
        private final int row1, pos1, row2, pos2;

        public SwapMove(FLPSolution s, int row1, int pos1, int row2, int pos2) {
            this(s, row1, pos1, row2, pos2, swapCost(s, row1, pos1, row2, pos2));
        }

        public SwapMove(FLPSolution s, int row1, int pos1, int row2, int pos2, double cost) {
            super(s, cost);
            this.row1 = row1;
            this.pos1 = pos1;
            this.row2 = row2;
            this.pos2 = pos2;
        }

        public static double swapCost(FLPSolution solution, int row1, int pos1, int row2, int pos2) {
            // Antes de hacer el movimiento
            double before = solution.getScore();
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            // Do movement
            var temp = solution.rows[row1][pos1];
            solution.rows[row1][pos1] = solution.rows[row2][pos2];
            solution.rows[row2][pos2] = temp;

            // Despues de hacer el movimiento
            // todo: if row1 == row2 skip second call
            solution.updateCenters(row1);
            solution.updateCenters(row2);

            double after = solution.recalculateScore();

            // Undo movement
            temp = solution.rows[row1][pos1];
            solution.rows[row1][pos1] = solution.rows[row2][pos2];
            solution.rows[row2][pos2] = temp;

            // todo: if row1 == row2 skip second call
            solution.updateCenters(row1);
            solution.updateCenters(row2);

            // Al deshacer el coste deberia quedar igual
            assert DoubleComparator.equals(before, solution.recalculateScore());
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());

            return after - before;
        }

        public static double consecutiveSwapCost(FLPSolution solution, int rowIdx, int leftIndex, int rightIndex) {
            int rowSize = solution.rowSize[rowIdx];
            var rowData = solution.rows[rowIdx];
            var instance = solution.getInstance();

            assert leftIndex < rightIndex : String.format("Left index (%s) must be strictly smaller than right index (%s)", leftIndex, rightIndex);
            assert rightIndex < rowSize : String.format("Out of bounds, max is %s, given %s", rowSize, rightIndex);
            assert leftIndex >= 0 : String.format("Out of bounds, min is %s, given %s", 0, leftIndex);

            var leftFacility = rowData[leftIndex];
            var rightFacility = rowData[rightIndex];

            double changedAreaStart = solution.center[leftFacility] - instance.length(leftFacility) / 2.0D;
            double newLeftCenter = changedAreaStart + instance.length(rightFacility) / 2.0D;
            double newRightCenter = changedAreaStart + instance.length(rightFacility) + instance.length(leftFacility) / 2.0D;


            // Distance moved, used absolute values
            double distanceChangeRight2Left = solution.center[rightFacility] - newLeftCenter;
            double distanceChangeLeft2Right = newRightCenter - solution.center[leftFacility];
            double costChange = 0;

            // AREAS EXPLANATION
            // 0 ----------------> AREA 3  ----------------> RowSize
            // 0 ----------------> AREA 3  ----------------> RowSize
            // 0 --> AREA 1 | index | index + 1 | AREA 2 --> RowSize
            // 0 ----------------> AREA 3  ----------------> RowSize
            // 0 ----------------> AREA 3  ----------------> RowSize

            // Area 1
            for (int i = 0; i < leftIndex; i++) {
                var otherbox = rowData[i];
                // Left box moves right increases cost
                costChange += distanceChangeLeft2Right * instance.flow(leftFacility, otherbox);
                // Right cost moves left decreases cost
                costChange -= distanceChangeRight2Left * instance.flow(rightFacility, otherbox);
            }

            // Area 2
            for (int i = rightIndex + 1; i < rowSize; i++) {
                var otherbox = rowData[i];
                // Left box moves right decreases cost
                costChange -= distanceChangeLeft2Right * instance.flow(leftFacility, otherbox);
                // Right cost moves left increases cost
                costChange += distanceChangeRight2Left * instance.flow(rightFacility, otherbox);
            }

            // Area 3: All rows different from current
            for (int currentRow = 0; currentRow < solution.rows.length; currentRow++) {
                if (currentRow == rowIdx) continue; // Current row already calculated by Area 1 and Area 2
                for (int i = 0; i < solution.rowSize[currentRow]; i++) {
                    var otherFacility = solution.rows[currentRow][i];
                    double beforeLeftCost = Math.abs(solution.center[otherFacility] - solution.center[leftFacility]);
                    double afterLeftCost = Math.abs(solution.center[otherFacility] - newRightCenter);
                    costChange += (afterLeftCost - beforeLeftCost) * instance.flow(otherFacility, leftFacility);

                    double beforeRightCost = Math.abs(solution.center[otherFacility] - solution.center[rightFacility]);
                    double afterRightCost = Math.abs(solution.center[otherFacility] - newLeftCenter);
                    costChange += (afterRightCost - beforeRightCost) * instance.flow(otherFacility, rightFacility);
                }
            }

            // Do fake move
            solution.center[leftFacility] = newRightCenter;
            solution.center[rightFacility] = newLeftCenter;

            // COST IS NOT UPDATED, but dont need to right?
            rowData[leftIndex] = rightFacility;
            rowData[rightIndex] = leftFacility;

            return costChange;
        }

        @Override
        protected FLPSolution _execute(FLPSolution solution) {
            solution.cachedScore += delta;
            var temp = solution.rows[row1][pos1];
            solution.rows[row1][pos1] = solution.rows[row2][pos2];
            solution.rows[row2][pos2] = temp;

            // todo: if row1 == row2 skip second call
            solution.updateCenters(row1);
            solution.updateCenters(row2);

            return solution;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SwapMove swapMove = (SwapMove) o;
            return row1 == swapMove.row1 && pos1 == swapMove.pos1 && row2 == swapMove.row2 && pos2 == swapMove.pos2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row1, pos1, row2, pos2);
        }
    }
}

