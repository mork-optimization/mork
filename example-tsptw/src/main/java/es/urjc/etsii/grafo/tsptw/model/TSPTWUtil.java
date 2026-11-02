package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class TSPTWUtil {
    // Change before compilation: false reproduces C; true uses each customer's deadline.
    private static final boolean FIX_REPAIR_DEADLINE_INDEX = false;

    private TSPTWUtil() {}

    public static void requireMinimumSize(TSPTWInstance instance) {
        if (instance.n() < 4) {
            throw new IllegalArgumentException("TSPTW search requires at least 4 nodes, including the depot");
        }
    }

    public static <T> List<T> distinctComponents(List<T> components, int min, int max) {
        var copy = List.copyOf(components);
        if (copy.size() < min || copy.size() > max) {
            throw new IllegalArgumentException("Expected between " + min + " and " + max + " components");
        }
        var types = new HashSet<Class<?>>();
        for (var component : copy) {
            if (!types.add(component.getClass())) {
                throw new IllegalArgumentException("Repeated component: " + component.getClass().getSimpleName());
            }
        }
        return copy;
    }

    public static void reinsert(int[] values, int from, int to) {
        int value = values[from];
        if (from < to) {
            System.arraycopy(values, from + 1, values, from, to - from);
        } else if (from > to) {
            System.arraycopy(values, to, values, to + 1, from - to);
        }
        values[to] = value;
    }

    public static boolean isPermutation(int[] values, int length) {
        if (length < 0 || length > values.length) return false;
        boolean[] seen = new boolean[length];
        for (int i = 0; i < length; i++) {
            int value = values[i];
            if (value < 0 || value >= length || seen[value]) return false;
            seen[value] = true;
        }
        return true;
    }

    /** Forward Fisher-Yates, matching native generate_array/generate_vector and libc++ random_shuffle. */
    public static void shuffle(int[] values) {
        var random = RandomManager.getRandom();
        for (int i = 0; i < values.length - 1; i++) {
            int j = i + random.nextInt(values.length - i);
            int value = values[i];
            values[i] = values[j];
            values[j] = value;
        }
    }

    public static void shuffle(List<?> values) {
        var random = RandomManager.getRandom();
        for (int i = 0; i < values.size() - 1; i++) {
            Collections.swap(values, i, i + random.nextInt(values.size() - i));
        }
    }

    /** Describes the compiled repair policy for logs and native replay fixtures. */
    public static String repairDeadlineIndex() {
        return FIX_REPAIR_DEADLINE_INDEX ? "CORRECTED" : "ORIGINAL_C";
    }

    /** Candidate selection only; actual time-window validation always indexes by customer. */
    public static boolean isLateForRepair(TSPTWSolution solution, int position) {
        int index = FIX_REPAIR_DEADLINE_INDEX ? solution.permutation[position] : position;
        return solution._makespan[position] > solution.window_end[index];
    }

    public static List<Integer> shuffledPositions(TSPTWSolution solution, boolean violated) {
        var positions = new java.util.ArrayList<Integer>();
        for (int i = 1; i < solution.getInstance().n(); i++) {
            if (isLateForRepair(solution, i) == violated) positions.add(i);
        }
        shuffle(positions);
        return positions;
    }
}
