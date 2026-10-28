package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

public class BMSSCGRASPConstructor extends Constructive<BMSSCSolution, BMSSCInstance> {
    private final double alpha;

    @AutoconfigConstructor
    public BMSSCGRASPConstructor(@RealParam(min = 0, max = 1) double alpha) {
        if (!Double.isFinite(alpha) || alpha < 0 || alpha > 1) throw new IllegalArgumentException("Invalid alpha: " + alpha);
        this.alpha = alpha;
    }

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        var instance = solution.getInstance();
        if (solution.getNotAssignedPoints().size() != instance.n) {
            throw new IllegalArgumentException("Construction requires an empty solution");
        }
        // Prepare a complete fallback so cancellation does not require another quota rebuild.
        var fallback = TimeControl.isEnabled() ? completeQuotas(solution) : null;
        if (fallback != null) {
            assert Context.validate(fallback);
        }
        var result = BMSSCUtil.withPartialSolution(() -> {
            seed(solution);
            if (!TimeControl.isTimeUp() && !solution.getNotAssignedPoints().isEmpty()) {
                // Reuse primitive buffers instead of rebuilding n*k move objects after every assignment.
                var costs = new double[Math.multiplyExact(instance.n, instance.k)];
                var candidates = new int[costs.length];
                while (!solution.getNotAssignedPoints().isEmpty() && !TimeControl.isTimeUp()) {
                    int candidate = select(solution, costs, candidates);
                    if (candidate < 0) break;
                    new AssignMove(solution, candidate % instance.n, candidate / instance.n).execute(solution);
                }
            }
            if (fallback != null && !solution.feasibleClusterSizes()) return fallback;
            return completeQuotas(solution);
        });
        if (!result.feasibleClusterSizes()) throw new IllegalStateException("Construction did not fill cluster quotas");
        assert result == fallback || Context.validate(result);
        Metrics.addCurrentObjectives(result);
        return result;
    }

    private void seed(BMSSCSolution solution) {
        if (TimeControl.isTimeUp()) return;
        var instance = solution.getInstance();
        var seeds = new int[instance.k];
        seeds[0] = RandomManager.getRandom().nextInt(instance.n);
        new AssignMove(solution, seeds[0], 0).execute(solution);
        for (int cluster = 1; cluster < instance.k; cluster++) {
            double maximum = Double.NEGATIVE_INFINITY;
            int chosen = -1;
            for (int point = 0; point < instance.n; point++) {
                if (TimeControl.isTimeUp()) return;
                if (solution.isAssigned(point)) continue;
                double minimum = Double.POSITIVE_INFINITY;
                for (int c = 0; c < cluster; c++) {
                    minimum = Math.min(minimum, instance.distance(seeds[c], point));
                }
                if (minimum > maximum) {
                    maximum = minimum;
                    chosen = point;
                }
            }
            seeds[cluster] = chosen;
            new AssignMove(solution, chosen, cluster).execute(solution);
        }
    }

    private int select(BMSSCSolution solution, double[] costs, int[] candidates) {
        var instance = solution.getInstance();
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (int cluster = 0; cluster < instance.k; cluster++) {
            if (TimeControl.isTimeUp()) return -1;
            if (solution.isFullCluster(cluster)) continue;
            for (int point = 0; point < instance.n; point++) {
                if ((point & 63) == 0 && TimeControl.isTimeUp()) return -1;
                if (solution.isAssigned(point)) continue;
                double cost = solution.assignDelta(point, cluster);
                costs[cluster * instance.n + point] = cost;
                min = Math.min(min, cost);
                max = Math.max(max, cost);
            }
        }
        double limit = min + alpha * (max - min);
        int count = 0;
        for (int cluster = 0; cluster < instance.k; cluster++) {
            if (TimeControl.isTimeUp()) return -1;
            if (solution.isFullCluster(cluster)) continue;
            for (int point = 0; point < instance.n; point++) {
                if ((point & 63) == 0 && TimeControl.isTimeUp()) return -1;
                if (solution.isAssigned(point)) continue;
                int candidate = cluster * instance.n + point;
                if (Main.OBJ.isBetterOrEqual(costs[candidate], limit)) candidates[count++] = candidate;
            }
        }
        return candidates[RandomManager.getRandom().nextInt(count)];
    }

    private BMSSCSolution completeQuotas(BMSSCSolution solution) {
        // Cancellation must still return a complete feasible solution, without another greedy scan.
        if (solution.feasibleClusterSizes()) return solution;
        var instance = solution.getInstance();
        int[] assignments = solution.getPointAssignments();
        int[] sizes = new int[instance.k];
        for (int c = 0; c < instance.k; c++) sizes[c] = solution.getClusterSize(c);
        int cluster = 0;
        for (int point = 0; point < instance.n; point++) {
            if (solution.isAssigned(point)) continue;
            while (sizes[cluster] == instance.getClusterSize(cluster)) cluster++;
            assignments[point] = cluster;
            sizes[cluster]++;
        }
        return new BMSSCSolution(instance, assignments);
    }

    @Override
    public String toString() { return "BMSSCGRASPConstructor{alpha=" + alpha + "}"; }
}
