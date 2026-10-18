package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Arrays;

import static es.urjc.etsii.grafo.bmssc.util.BMSSCUtil.contribution;

public class BMSSCSolutionValidator extends SolutionValidator<BMSSCSolution, BMSSCInstance> {
    @Override
    public ValidationResult validate(BMSSCSolution solution) {
        // Final validation always recomputes expectations from an empty state.
        return validateState(solution, true, new CacheValidation(solution.getInstance()));
    }

    static ValidationResult validateState(BMSSCSolution solution, boolean complete, CacheValidation expected) {
        var result = ValidationResult.ok();
        var instance = solution.getInstance();
        int[] membership = new int[instance.n];
        int[] assignments = new int[instance.n];
        Arrays.fill(assignments, -1);
        int[] sizes = new int[instance.k];
        for (int c = 0; c < instance.k; c++) {
            var cluster = solution.getCluster(c);
            int size = sizes[c] = cluster.size();
            if (size != solution.getClusterSize(c)) result.addFailure("Incorrect cardinality cache for cluster " + c);
            if (size > solution.getClusterCapacity(c)) result.addFailure("Capacity exceeded for cluster " + c);
            if (complete && size != instance.getClusterSize(c)) {
                result.addFailure("Cluster " + c + ": expected " + instance.getClusterSize(c) + " points, got " + size);
            }
            if (complete && solution.getClusterCapacity(c) != instance.getClusterSize(c)) {
                result.addFailure("Capacity constraint still relaxed for cluster " + c);
            }
            for (int p : cluster) {
                membership[p]++;
                assignments[p] = c;
                if (solution.clusterOf(p) != c) result.addFailure("Incorrect cluster lookup for point " + p);
            }
        }
        expected.update(instance, assignments, sizes);
        double cost = 0;
        for (int c = 0; c < instance.k; c++) {
            double pairSum = 0;
            for (int p = 0; p < instance.n; p++) {
                double sum = expected.distances[p][c];
                // Halve each endpoint's contribution before summing to avoid doubling overflow.
                if (assignments[p] == c) pairSum += sum / 2;
                double actual = solution.getPointClusterDistance(p, c);
                if (!Double.isFinite(sum) || !Double.isFinite(actual) || !DoubleComparator.equals(sum, actual)) {
                    result.addFailure("Distance cache for point " + p + ", cluster " + c
                            + ": expected " + sum + ", got " + actual);
                }
            }
            checkValue(result, "Pair sum for cluster " + c, pairSum, solution.getPairSum(c));
            cost += contribution(pairSum, sizes[c]);
        }
        var unassignedPoints = solution.getNotAssignedPoints();
        for (int p = 0; p < instance.n; p++) {
            boolean unassigned = unassignedPoints.contains(p);
            if (membership[p] > 1 || (membership[p] == 0) != unassigned
                    || unassigned != (solution.clusterOf(p) == -1)) {
                result.addFailure("Inconsistent membership for point " + p);
            }
            if (complete && membership[p] != 1) result.addFailure("Point " + p + " must be assigned exactly once");
        }
        checkValue(result, "Cost", cost, solution.getCost());
        return result;
    }

    /**
     * Independent expectations derived from cluster membership, never from solution caches
     * or move deltas. Updating changed memberships avoids a cubic full reconstruction while
     * constructing a solution; every cached value is still checked after every move.
     */
    static final class CacheValidation {
        private final int[] assignments;
        private final double[][] distances;

        CacheValidation(BMSSCInstance instance) {
            assignments = new int[instance.n];
            Arrays.fill(assignments, -1);
            distances = new double[instance.n][instance.k];
        }

        void update(BMSSCInstance instance, int[] current, int[] sizes) {
            for (int p = 0; p < instance.n; p++) {
                int previous = assignments[p], next = current[p];
                if (previous == next) continue;
                for (int q = 0; q < instance.n; q++) {
                    double distance = instance.distance(p, q);
                    if (previous >= 0) distances[q][previous] -= distance;
                    if (next >= 0) distances[q][next] += distance;
                }
                assignments[p] = next;
            }
            for (int c = 0; c < instance.k; c++) {
                if (sizes[c] != 0) continue;
                for (int p = 0; p < instance.n; p++) distances[p][c] = 0;
            }
        }
    }

    private static void checkValue(ValidationResult result, String name, double expected, double actual) {
        if (!Double.isFinite(expected) || !Double.isFinite(actual) || !DoubleComparator.equals(expected, actual)) {
            result.addFailure(name + ": expected " + expected + ", got " + actual);
        }
    }
}
