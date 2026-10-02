package es.urjc.etsii.grafo.mreflp.create;

import es.urjc.etsii.grafo.mreflp.alg.LearningMatrixNew;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Shared ordering and capacity-aware completion of fresh or partial assignments. */
public final class MREFLPReconstructionUtil {
    private MREFLPReconstructionUtil() {}

    public static int[] facilityOrder(MREFLPInstance instance, FacilityOrderNew mode) {
        int[] order = new int[instance.n()];
        for (int v = 0; v < order.length; v++) order[v] = v;
        ArrayUtil.shuffle(order);
        if (mode == FacilityOrderNew.FLOW_DESCENDING) {
            long[] degree = new long[order.length];
            for (int v = 0; v < order.length; v++) {
                for (int u = 0; u < order.length; u++) degree[v] += instance.flow(u, v);
            }
            // Stable insertion sort preserves the shuffled order among equal degrees.
            for (int i = 1; i < order.length; i++) {
                int v = order[i], j = i;
                while (j > 0 && degree[order[j - 1]] < degree[v]) {
                    order[j] = order[j - 1];
                    j--;
                }
                order[j] = v;
            }
        }
        return order;
    }

    public static MREFLPSolution fromAssignments(MREFLPInstance instance, int[] assignments) {
        if (assignments.length != instance.n()) throw new IllegalArgumentException("Wrong assignment length");
        var result = new MREFLPSolution(instance);
        for (int v = 0; v < assignments.length; v++) {
            if (assignments[v] >= 0) result.assign(v, assignments[v]);
        }
        result.notifyUpdate();
        return result;
    }

    public static MREFLPSolution complete(MREFLPSolution solution, ConstructionPolicyNew policy,
                                         FacilityOrderNew orderMode, double rcl,
                                         LearningMatrixNew learning, double epsilon) {
        var instance = solution.getInstance();
        boolean incomplete = false;
        for (int v = 0; v < instance.n(); v++) if (solution.group(v) < 0) { incomplete = true; break; }
        if (!incomplete) { solution.notifyUpdate(); return solution; }
        var random = RandomManager.getRandom();
        int[] order = facilityOrder(instance, orderMode);
        long[][] groupFlows = null;
        if (policy != ConstructionPolicyNew.RANDOM && !TimeControl.isTimeUp()) {
            groupFlows = new long[instance.n()][instance.groups()];
            for (int u = 0; u < instance.n(); u++) {
                if (solution.group(u) >= 0) addGroupFlow(instance, groupFlows, u, solution.group(u));
            }
        }
        for (int v : order) {
            if (solution.group(v) >= 0) continue;
            // Finishing a feasible assignment takes precedence over an expired deadline.
            boolean expired = TimeControl.isTimeUp();
            boolean informed = !expired && learning != null && random.nextDouble() < epsilon;
            long[] marginal = !expired && !informed && groupFlows != null ? marginalCosts(groupFlows[v]) : null;
            int chosen;
            if (informed) {
                double best = -Double.MAX_VALUE;
                chosen = -1;
                int ties = 0;
                for (int g = 0; g < instance.groups(); g++) {
                    if (solution.occupancy(g) == instance.capacity()) continue;
                    double value = learning.value(v, g);
                    if (value > best) { best = value; chosen = g; ties = 1; }
                    else if (value == best && random.nextInt(++ties) == 0) chosen = g;
                }
            } else {
                double alpha = policy == ConstructionPolicyNew.GREEDY ? 0 : rcl;
                long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
                if (marginal != null) {
                    for (int g = 0; g < instance.groups(); g++) {
                        if (solution.occupancy(g) == instance.capacity()) continue;
                        min = Math.min(min, marginal[g]);
                        max = Math.max(max, marginal[g]);
                    }
                }
                double threshold = min + alpha * ((double) max - min);
                chosen = -1;
                int ties = 0;
                for (int g = 0; g < instance.groups(); g++) {
                    if (solution.occupancy(g) == instance.capacity()) continue;
                    if (marginal != null && marginal[g] > threshold) continue;
                    if (random.nextInt(++ties) == 0) chosen = g;
                }
            }
            solution.assign(v, chosen);
            if (groupFlows != null && !TimeControl.isTimeUp()) addGroupFlow(instance, groupFlows, v, chosen);
        }
        solution.notifyUpdate();
        return solution;
    }

    /** All group marginal costs from aggregated flows, in O(k). */
    public static long[] marginalCosts(long[] groupFlows) {
        long total = 0, current = 0, left = 0;
        for (int g = 0; g < groupFlows.length; g++) {
            total += groupFlows[g];
            current += groupFlows[g] * g;
        }
        long[] costs = new long[groupFlows.length];
        for (int g = 0; g < costs.length; g++) {
            costs[g] = current;
            left += groupFlows[g];
            current += 2 * left - total;
        }
        return costs;
    }

    private static void addGroupFlow(MREFLPInstance instance, long[][] groupFlows, int assigned, int group) {
        for (int v = 0; v < instance.n(); v++) {
            groupFlows[v][group] += instance.flow(v, assigned);
        }
    }
}
