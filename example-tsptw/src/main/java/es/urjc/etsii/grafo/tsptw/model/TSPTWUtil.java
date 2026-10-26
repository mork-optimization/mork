package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.HashSet;
import java.util.List;

public final class TSPTWUtil {
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

    public static List<Integer> shuffledPositions(TSPTWSolution solution, boolean violated) {
        var positions = new java.util.ArrayList<Integer>();
        for (int i = 1; i < solution.getInstance().n(); i++) {
            if (solution.isLateAt(i) == violated) positions.add(i);
        }
        CollectionUtil.shuffle(positions);
        return positions;
    }
}
