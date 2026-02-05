package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.ArrayList;
import java.util.Objects;

public class FLPOptNeigh extends Neighborhood<FLPOptNeigh.OptMove, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<OptMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        var moves = new ArrayList<OptMove>();
        for (int row = 0; row < solution.nRows(); row++) {
            if (solution.rowSize(row) <= 1) {
                continue;
            }
            int rowSize = solution.rowSize(row);
            for (int pos1 = 0; pos1 < rowSize - 1; pos1++) {
                for (int pos2 = pos1 + 1; pos2 < rowSize; pos2++) {
                    moves.add(new OptMove(solution, row, pos1, pos2));
                }
            }
        }
        return ExploreResult.fromList(moves);
    }

    public static class OptMove extends FLPMove {

        private final int row;
        private final int pos1;
        private final int pos2;

        public OptMove(FLPSolution s, int row, int pos1, int pos2) {
            this(s, row, pos1, pos2, twoOptCost(s, row, pos1, pos2));
        }

        public OptMove(FLPSolution s, int row, int pos1, int pos2, double cost) {
            super(s, cost);
            this.row = row;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        // Neighborhoods cost calculation and move execution
        private static double twoOptCost(FLPSolution solution, int row, int pos1, int pos2) {
            // Antes de hacer el movimiento
            double before = solution.partialCost(row, pos1, pos2);

            // Do movement
            ArrayUtil.reverse(solution.rows[row], pos1, pos2);

            // Despues de hacer el movimiento
            solution.updateCenters(row);
            double after = solution.partialCost(row, pos1, pos2);

            // Undo movement
            ArrayUtil.reverse(solution.rows[row], pos1, pos2);

            // Al deshacer el coste deberia quedar igual
            solution.updateCenters(row);
            assert DoubleComparator.equals(before, solution.partialCost(row, pos1, pos2));

            return after - before;
        }

        @Override
        protected FLPSolution _execute(FLPSolution solution) {
            solution.cachedScore += delta;
            ArrayUtil.reverse(solution.rows[row], pos1, pos2);
            solution.updateCenters(row);
            return solution;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            OptMove optMove = (OptMove) o;
            return row == optMove.row && pos1 == optMove.pos1 && pos2 == optMove.pos2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, pos1, pos2);
        }
    }
}

