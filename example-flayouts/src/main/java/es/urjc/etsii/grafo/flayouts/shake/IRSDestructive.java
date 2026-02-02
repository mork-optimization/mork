package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.flayouts.constructives.tetris.Piece;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.flayouts.model.SwapNeighborhood;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class IRSDestructive extends Destructive<FLPSolution, FLPInstance> {

    private static final Logger log = LoggerFactory.getLogger(IRSDestructive.class);

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        if (k != 1) {
            throw new IllegalArgumentException("IRSDestructive is designed to work with IteratedGreedy like methods, not VNS");
        }

        if (solution.nRows() != 2) {
            throw new IllegalArgumentException("IRSDestructive only tested for DRFP, current nRows = " + solution.nRows());
        }

        var instance = solution.getInstance();

        var touchingFacilities = getTouching(solution);
        if (touchingFacilities.isEmpty()) {
            log.debug(String.format("Possible bug: could not detect any facilities touching between rows. Skipping this iteration. Data: %s", Arrays.deepToString(solution.getRows())));
            return null;
        }

        var ids = toIds(solution);
        var item = CollectionUtil.pickRandom(touchingFacilities);

        assert item.originRow != item.destRow;

        int a_left_start = 0;
        int a_left_end = item.originPosition;
        int b_left_start = 0;
        int b_left_end = item.destPosition;
        int a_right_start = item.originPosition + 1;
        int a_right_end = solution.rowSize(item.originRow);
        int b_right_start = item.destPosition + 1;
        int b_right_end = solution.rowSize(item.destRow);

        // First piece: Data before the chosen touching facilities
        int[][] data1 = new int[][]{
                Arrays.copyOfRange(ids[item.originRow], a_left_start, a_left_end),
                Arrays.copyOfRange(ids[item.destRow], b_left_start, b_left_end)
        };

        // Second piece, the chosen touching facilities
        int[][] data2 = new int[][]{
                new int[]{ids[item.originRow][item.originPosition]},
                new int[]{ids[item.destRow][item.destPosition]}
        };

        // Third piece, data after the chosen touching facilities
        int[][] data3 = new int[][]{
                Arrays.copyOfRange(ids[item.originRow], a_right_start, a_right_end),
                Arrays.copyOfRange(ids[item.destRow], b_right_start, b_right_end)
        };

        var pieces = solution.getPendingPieces();
        pieces.add(new Piece(data1, FLPSolution.evaluate(instance, data1, 0.5), null, null)); // todo chapuza 0.5 hardcoded
        pieces.add(new Piece(data2, FLPSolution.evaluate(instance, data2, 0.5), null, null)); // todo chapuza 0.5 hardcoded
        pieces.add(new Piece(data3, FLPSolution.evaluate(instance, data3, 0.5), null, null)); // todo chapuza 0.5 hardcoded

        solution.deassignAll();
        return solution;
    }

    private int[][] toIds(FLPSolution solution) {
        var facilities = solution.getRows();
        int[][] result = new int[facilities.length][];
        for (int i = 0; i < facilities.length; i++) {
            int rowSize = solution.rowSize(i);
            result[i] = new int[rowSize];
            for (int j = 0; j < rowSize; j++) {
                result[i][j] = facilities[i][j].id;
            }
        }
        return result;
    }

    private ArrayList<Touch> getTouching(FLPSolution solution) {
        var touches = new ArrayList<Touch>();
        var data = solution.getRows();

        for (int row1 = 0; row1 < solution.nRows(); row1++) {
            for (int pos1 = 0; pos1 < solution.rowSize(row1); pos1++) {
                var f1 = data[row1][pos1];
                for (int row2 = row1 + 1; row2 < solution.nRows(); row2++) {
                    assert row1 != row2;
                    for (int pos2 = 0; pos2 < solution.rowSize(row2); pos2++) {
                        var f2 = data[row2][pos2];
                        if (f1.stacks(f2)) {
                            var t = new Touch(solution, row1, pos1, row2, pos2);
                            touches.add(t);
                        }
                    }
                }
            }
        }
        return touches;
    }

    @Override
    public String toString() {
        return "IRSDestructive{}";
    }

    private record Touch(FLPSolution solution, int originRow, int originPosition,
                         int destRow, int destPosition) {

        public SwapNeighborhood.SwapMove toSwap() {
            int index1 = solution.getPositionForRowIndex(originRow, originPosition);
            int index2 = solution.getPositionForRowIndex(destRow, destPosition);
            return new SwapNeighborhood.SwapMove(this.solution, index1, index2);
        }

        public int getId1() {
            return solution.getRows()[originRow][originPosition].id;
        }

        public int getId2() {
            return solution.getRows()[destRow][destPosition].id;
        }
    }


}
