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

    /** Negative when a is better: total lateness, then violated windows, then tour cost. */
    public static int compareForRepair(TSPTWSolution a, TSPTWSolution b) {
        int comparison = Double.compare(a.infeasibility(), b.infeasibility());
        if (comparison != 0) return comparison;
        comparison = Integer.compare(a.constraint_violations(), b.constraint_violations());
        if (comparison != 0) return comparison;
        return Double.compare(a.cost(), b.cost());
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

    public static <T> void reinsert(List<T> values, int from, int to) {
        var value = values.remove(from);
        values.add(to, value);
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
