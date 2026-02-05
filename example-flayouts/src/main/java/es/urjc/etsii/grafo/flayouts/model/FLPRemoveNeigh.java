package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.ArrayList;
import java.util.Objects;

import static es.urjc.etsii.grafo.flayouts.model.FLPSolution.FREE_SPACE;

public class FLPRemoveNeigh extends Neighborhood<FLPRemoveNeigh.RemoveMove, FLPSolution, FLPInstance> {

    @Override
    public ExploreResult<RemoveMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        var moves = new ArrayList<RemoveMove>();
        for (int row = 0; row < solution.nRows(); row++) {
            for (int pos = 0; pos < solution.rowSize(row); pos++) {
                moves.add(new RemoveMove(solution, row, pos));
            }
        }
        return ExploreResult.fromList(moves);
    }


    public static double removeCost(FLPSolution solution, int rowIdx, int pos) {
        var row = solution.rows[rowIdx];
        int f = row[pos];
        int rowSize = solution.rowSize[rowIdx];

        double before = solution.partialCost(rowIdx, pos, rowSize);

        // Do move
        solution.rowSize[rowIdx]--;
        System.arraycopy(row, pos + 1, row, pos, solution.rowSize[rowIdx] - pos); // Todo, no faltaria un -1 al length?
        row[solution.rowSize[rowIdx]] = FREE_SPACE;
        solution.updateCentersFrom(rowIdx, pos);

        double after = solution.partialCost(rowIdx, pos, rowSize -1);

        // Undo move
        System.arraycopy(row, pos, row, pos + 1, solution.rowSize[rowIdx] - pos);
        row[pos] = f;
        solution.rowSize[rowIdx]++;
        solution.updateCentersFrom(rowIdx, pos);

        // Al deshacer el coste deberia quedar igual
        assert DoubleComparator.equals(before, solution.partialCost(rowIdx, pos, rowSize - 1));

        return after - before;
    }

    public static class RemoveMove extends FLPMove {

        private final int row;
        private final int pos;

        public RemoveMove(FLPSolution solution, int row, int pos) {
            this(solution, row, pos, removeCost(solution, row, pos));
        }

        public RemoveMove(FLPSolution s, int row, int pos, double delta) {
            super(s, delta);
            this.row = row;
            this.pos = pos;
        }

        @Override
        protected FLPSolution _execute(FLPSolution solution) {
            var rows = solution.getRows();
            solution.rowSize[row]--;
            var newSize = solution.rowSize[row];
            System.arraycopy(rows[row], pos + 1, rows[row], pos, newSize - pos); // Todo, no faltaria un -1 al length?
            rows[row][newSize] = FREE_SPACE;
            solution.updateCentersFrom(row, pos);

            return solution;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RemoveMove that = (RemoveMove) o;
            return row == that.row && pos == that.pos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, pos);
        }
    }
}
