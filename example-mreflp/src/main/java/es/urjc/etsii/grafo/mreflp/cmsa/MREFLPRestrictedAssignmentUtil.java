package es.urjc.etsii.grafo.mreflp.cmsa;

import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Restricted domains and capacitated augmenting paths for a feasible CMSA incumbent. */
public final class MREFLPRestrictedAssignmentUtil {
    private MREFLPRestrictedAssignmentUtil() {}

    public static int[][] domains(MREFLPInstance instance, Set<MREFLPAssignmentNew> components) {
        boolean[][] allowed = new boolean[instance.n()][instance.groups()];
        for (var component : components) {
            if (component.facility() < 0 || component.facility() >= instance.n()
                    || component.group() < 0 || component.group() >= instance.groups()) {
                throw new IllegalArgumentException("Invalid restricted assignment " + component);
            }
            allowed[component.facility()][component.group()] = true;
        }
        int[][] domains = new int[instance.n()][];
        for (int v = 0; v < domains.length; v++) {
            int count = 0;
            for (boolean value : allowed[v]) if (value) count++;
            domains[v] = new int[count];
            int index = 0;
            for (int g = 0; g < allowed[v].length; g++) if (allowed[v][g]) domains[v][index++] = g;
        }
        return domains;
    }

    /** Returns null on infeasibility or cancellation; successful assignments use only the domains. */
    public static int[] feasible(MREFLPInstance instance, int[][] domains, int[] order, BooleanSupplier cancelled) {
        int[] assignments = new int[instance.n()], counts = new int[instance.groups()];
        Arrays.fill(assignments, -1);
        for (int v : order) {
            if (!augment(v, instance.capacity(), domains, assignments, counts, new boolean[counts.length], cancelled)) return null;
        }
        return assignments;
    }

    private static boolean augment(int v, int capacity, int[][] domains, int[] assignments, int[] counts,
                                   boolean[] seen, BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) return false;
        for (int g : domains[v]) {
            if (seen[g]) continue;
            seen[g] = true;
            if (counts[g] < capacity) { assign(v, g, assignments, counts); return true; }
            for (int u = 0; u < assignments.length; u++) {
                if (assignments[u] == g && augment(u, capacity, domains, assignments, counts, seen, cancelled)) {
                    assign(v, g, assignments, counts);
                    return true;
                }
            }
        }
        return false;
    }

    private static void assign(int v, int g, int[] assignments, int[] counts) {
        if (assignments[v] >= 0) counts[assignments[v]]--;
        assignments[v] = g;
        counts[g]++;
    }
}
