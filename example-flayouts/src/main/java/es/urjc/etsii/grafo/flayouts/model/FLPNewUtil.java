package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.stream.StreamSupport;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewMove.Kind.*;

/** Shared state and exact delta operations for new components; original helpers are untouched. */
public final class FLPNewUtil {
    private FLPNewUtil() {}

    public record MoveSpace(long count, LongFunction<FLPNewMove> at) {}

    public static int[][] rows(FLPSolution s) {
        int[][] rows = new int[s.nRows()][];
        for (int r = 0; r < rows.length; r++) rows[r] = Arrays.copyOf(s.rows[r], s.rowSize(r));
        return rows;
    }

    public static List<Integer> missing(FLPSolution s) {
        var ids = new ArrayList<Integer>();
        for (int f = 0; f < s.getInstance().nFacilities(); f++) if (s.notAssignedFacilities.contains(f)) ids.add(f);
        return ids;
    }

    public static FLPSolution.Coords[] positions(FLPSolution s) {
        var positions = new FLPSolution.Coords[s.nAssigned()];
        int i = 0;
        for (int r = 0; r < s.nRows(); r++) for (int p = 0; p < s.rowSize(r); p++) positions[i++] = new FLPSolution.Coords(r, p);
        return positions;
    }

    public static FLPSolution.Coords[] gaps(FLPSolution s) {
        var positions = new FLPSolution.Coords[s.nAssigned() + s.nRows()];
        int i = 0;
        for (int r = 0; r < s.nRows(); r++) for (int p = 0; p <= s.rowSize(r); p++) positions[i++] = new FLPSolution.Coords(r, p);
        return positions;
    }

    public static FLPSolution.Coords locate(FLPSolution s, int facility) {
        return locate(rows(s), facility);
    }

    private static FLPSolution.Coords locate(int[][] rows, int facility) {
        for (int r = 0; r < rows.length; r++) for (int p = 0; p < rows[r].length; p++) {
            if (rows[r][p] == facility) return new FLPSolution.Coords(r, p);
        }
        throw new IllegalArgumentException("Facility is not assigned: " + facility);
    }

    public static FLPNewMove add(FLPSolution s, int facility, int row, int pos, boolean fast) {
        return new FLPNewMove(s, ADD, new int[]{facility}, row, anchor(s, row, pos), false, fast);
    }

    public static FLPNewMove remove(FLPSolution s, int... facilities) {
        int[] sorted = facilities.clone();
        Arrays.sort(sorted);
        return new FLPNewMove(s, REMOVE, sorted, -1, -1, false, true);
    }

    public static FLPNewMove swap(FLPSolution s, int first, int second, boolean fast) {
        return new FLPNewMove(s, SWAP, new int[]{Math.min(first, second), Math.max(first, second)}, -1, -1, false, fast);
    }

    public static FLPNewMove reverse(FLPSolution s, int first, int second, boolean fast) {
        return new FLPNewMove(s, REVERSE, new int[]{Math.min(first, second), Math.max(first, second)}, -1, -1, false, fast);
    }

    /** Destination position is a gap in the original row, before removing the source block. */
    public static FLPNewMove relocate(FLPSolution s, int row, int pos, int length, int targetRow, int gap, boolean reverse, boolean fast) {
        if (length < 1 || pos < 0 || pos + length > s.rowSize(row)) throw new IllegalArgumentException("Invalid block");
        if (row == targetRow && gap >= pos && gap <= pos + length) throw new IllegalArgumentException("Destination is inside the block");
        return new FLPNewMove(s, RELOCATE, Arrays.copyOfRange(s.rows[row], pos, pos + length), targetRow,
                anchor(s, targetRow, gap), reverse, fast);
    }

    private static int anchor(FLPSolution s, int row, int pos) {
        if (row < 0 || row >= s.nRows() || pos < 0 || pos > s.rowSize(row)) throw new IllegalArgumentException("Invalid insertion gap");
        return pos == s.rowSize(row) ? -1 : s.rows[row][pos];
    }

    static int[][] changedRows(FLPSolution s, FLPNewMove.Kind kind, int[] ids, int row, int anchor, boolean reverse) {
        if (ids.length == 0) throw new IllegalArgumentException("Empty move");
        var data = rows(s);
        switch (kind) {
            case ADD -> {
                if (ids.length != 1 || !s.notAssignedFacilities.contains(ids[0])) throw new IllegalArgumentException("Facility is already assigned");
                var target = destination(data, row, anchor);
                data[target.row()] = insert(data[target.row()], target.pos(), ids);
            }
            case REMOVE -> {
                for (int id : ids) {
                    var p = locate(data, id);
                    data[p.row()] = removeRange(data[p.row()], p.pos(), 1);
                }
            }
            case SWAP, REVERSE -> {
                if (ids.length != 2 || ids[0] == ids[1]) throw new IllegalArgumentException("Two different facilities required");
                var a = locate(data, ids[0]);
                var b = locate(data, ids[1]);
                if (kind == SWAP) {
                    data[a.row()][a.pos()] = ids[1];
                    data[b.row()][b.pos()] = ids[0];
                } else {
                    if (a.row() != b.row()) throw new IllegalArgumentException("Reversal endpoints must share a row");
                    ArrayUtil.reverse(data[a.row()], Math.min(a.pos(), b.pos()), Math.max(a.pos(), b.pos()));
                }
            }
            case RELOCATE -> {
                var origin = locate(data, ids[0]);
                if (origin.pos() + ids.length > data[origin.row()].length) throw new IllegalArgumentException("Block no longer contiguous");
                for (int i = 0; i < ids.length; i++) {
                    if (data[origin.row()][origin.pos() + i] != ids[i]) throw new IllegalArgumentException("Block no longer contiguous");
                    if (ids[i] == anchor) throw new IllegalArgumentException("Anchor is inside block");
                }
                data[origin.row()] = removeRange(data[origin.row()], origin.pos(), ids.length);
                var target = destination(data, row, anchor);
                if (origin.row() == target.row() && origin.pos() == target.pos() && !reverse) throw new IllegalArgumentException("Relocation is a no-op");
                int[] block = ids.clone();
                if (reverse) ArrayUtil.reverse(block);
                data[target.row()] = insert(data[target.row()], target.pos(), block);
            }
        }
        return data;
    }

    private static FLPSolution.Coords destination(int[][] rows, int row, int anchor) {
        if (anchor >= 0) return locate(rows, anchor);
        if (row < 0 || row >= rows.length) throw new IllegalArgumentException("Invalid destination row");
        return new FLPSolution.Coords(row, rows[row].length);
    }

    private static int[] insert(int[] row, int pos, int[] ids) {
        int[] result = new int[row.length + ids.length];
        System.arraycopy(row, 0, result, 0, pos);
        System.arraycopy(ids, 0, result, pos, ids.length);
        System.arraycopy(row, pos, result, pos + ids.length, row.length - pos);
        return result;
    }

    private static int[] removeRange(int[] row, int pos, int length) {
        int[] result = new int[row.length - length];
        System.arraycopy(row, 0, result, 0, pos);
        System.arraycopy(row, pos + length, result, pos, row.length - pos - length);
        return result;
    }

    public static double[] centers(FLPInstance instance, int[][] rows) {
        double[] result = new double[instance.nFacilities()];
        Arrays.fill(result, FLPSolution.UNKNOWN_CENTER);
        for (int[] row : rows) {
            double left = 0;
            for (int facility : row) {
                result[facility] = left + instance.length(facility) / 2.0;
                left += instance.length(facility);
            }
        }
        return result;
    }

    private static double pairCost(FLPInstance instance, double[] centers, int a, int b) {
        if (centers[a] == FLPSolution.UNKNOWN_CENTER || centers[b] == FLPSolution.UNKNOWN_CENTER) return 0;
        return Math.abs(centers[a] - centers[b]) * (instance.flow(a, b) + (double) instance.flow(b, a)) / 2;
    }

    public static double score(FLPInstance instance, int[][] rows) {
        return score(instance, centers(instance, rows));
    }

    private static double score(FLPInstance instance, double[] centers) {
        double score = 0;
        for (int a = 0; a < centers.length; a++) for (int b = a + 1; b < centers.length; b++) score += pairCost(instance, centers, a, b);
        return score;
    }

    static double delta(FLPSolution s, FLPNewMove.Kind kind, int[] ids, int row, int anchor, boolean reverse, boolean fast) {
        var instance = s.getInstance();
        double[] after = centers(instance, changedRows(s, kind, ids, row, anchor, reverse));
        if (!fast) return score(instance, after) - s.getScore();
        boolean[] changed = new boolean[after.length];
        for (int f = 0; f < after.length; f++) changed[f] = after[f] != s.center[f];
        double delta = 0;
        for (int a = 0; a < after.length; a++) {
            if (!changed[a]) continue;
            for (int b = 0; b < after.length; b++) {
                if (a == b || (changed[b] && b < a)) continue;
                delta += pairCost(instance, after, a, b) - pairCost(instance, s.center, a, b);
            }
        }
        return delta;
    }

    static void install(FLPSolution s, int[][] data, double score) {
        s.notAssignedFacilities.clear();
        for (int f = 0; f < s.getInstance().nFacilities(); f++) s.notAssignedFacilities.add(f);
        s.assignedFacilities = 0;
        for (int r = 0; r < data.length; r++) {
            Arrays.fill(s.rows[r], FLPSolution.FREE_SPACE);
            System.arraycopy(data[r], 0, s.rows[r], 0, data[r].length);
            s.rowSize[r] = data[r].length;
            s.assignedFacilities += data[r].length;
            for (int f : data[r]) s.notAssignedFacilities.remove(f);
        }
        s.center = centers(s.getInstance(), data);
        s.cachedScore = score;
    }

    public static ExploreResult<FLPNewMove, FLPSolution, FLPInstance> explore(MoveSpace space, boolean lazy) {
        var cursor = new Spliterators.AbstractSpliterator<FLPNewMove>(space.count(), Spliterator.ORDERED | Spliterator.NONNULL) {
            long index;
            @Override
            public boolean tryAdvance(Consumer<? super FLPNewMove> action) {
                while (index < space.count() && !TimeControl.isTimeUp()) {
                    var move = space.at().apply(index++);
                    if (move != null) { action.accept(move); return true; }
                }
                return false;
            }
        };
        if (lazy) return ExploreResult.fromStream(StreamSupport.stream(cursor, false));
        var list = new ArrayList<FLPNewMove>();
        while (cursor.tryAdvance(list::add)) { /* evaluate the complete list for the baseline */ }
        return ExploreResult.fromList(list);
    }

    public static MoveSpace relocationSpace(FLPSolution s, int length, boolean reverse, FLPBlockRelocateNeighNew.Scope scope, boolean fast) {
        var positions = positions(s);
        var gaps = gaps(s);
        return new MoveSpace((long) positions.length * gaps.length, index -> {
            var origin = positions[(int) (index / gaps.length)];
            var target = gaps[(int) (index % gaps.length)];
            if (origin.pos() + length > s.rowSize(origin.row())) return null;
            boolean sameRow = origin.row() == target.row();
            if ((scope == FLPBlockRelocateNeighNew.Scope.WITHIN && !sameRow)
                    || (scope == FLPBlockRelocateNeighNew.Scope.BETWEEN && sameRow)) return null;
            if (sameRow && target.pos() >= origin.pos() && target.pos() <= origin.pos() + length) return null;
            return relocate(s, origin.row(), origin.pos(), length, target.row(), target.pos(), reverse, fast);
        });
    }

    public static Optional<FLPNewMove> randomMove(MoveSpace space) {
        if (space.count() == 0 || TimeControl.isTimeUp()) return Optional.empty();
        var random = RandomManager.getRandom();
        for (int attempt = 0; attempt < 64 && !TimeControl.isTimeUp(); attempt++) {
            var move = space.at().apply(random.nextLong(space.count()));
            if (move != null) return Optional.of(move);
        }
        // Sparse spaces still return a move if one exists; never retry indefinitely.
        try (var moves = explore(space, true).moves()) { return moves.findFirst(); }
    }

    public static FLPNewMove appendMissing(FLPSolution s) {
        int facility = missing(s).getFirst();
        int row = 0;
        for (int r = 1; r < s.nRows(); r++) if (s.rowSize(r) < s.rowSize(row)) row = r;
        return add(s, facility, row, s.rowSize(row), true);
    }

    /** Finish repair cheaply even after a deadline; never return an incomplete incumbent. */
    public static FLPSolution completeByAppend(FLPSolution s) {
        while (!s.notAssignedFacilities.isEmpty()) appendMissing(s).execute(s);
        return s;
    }

    public static void probability(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException(name + " must be in [0,1]");
    }

    public static int removalCount(FLPSolution s, double ratio, int k, boolean scaleWithK) {
        if (s.nAssigned() == 0) return 0;
        double multiplier = scaleWithK ? Math.max(1, k) : 1;
        return (int) Math.min(s.nAssigned(), Math.max(1L, Math.round(s.getInstance().nFacilities() * ratio * multiplier)));
    }
}
