package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.util.DoubleComparator;

import static es.urjc.etsii.grafo.bmssc.util.BMSSCUtil.contribution;

public class BMSSCSolutionValidator extends SolutionValidator<BMSSCSolution, BMSSCInstance> {
    @Override
    public ValidationResult validate(BMSSCSolution solution) {
        return validateState(solution, true);
    }

    static ValidationResult validateState(BMSSCSolution solution, boolean complete) {
        var result = ValidationResult.ok();
        var instance = solution.getInstance();
        int[] membership = new int[instance.n];
        double cost = 0;
        for (int c = 0; c < instance.k; c++) {
            var cluster = solution.getCluster(c);
            int size = cluster.size();
            if (size != solution.getClusterSize(c)) result.addFailure("Incorrect cardinality cache for cluster " + c);
            if (size > solution.getClusterCapacity(c)) result.addFailure("Capacity exceeded for cluster " + c);
            if (complete && size != instance.getClusterSize(c)) {
                result.addFailure("Cluster " + c + ": expected " + instance.getClusterSize(c) + " points, got " + size);
            }
            if (complete && solution.getClusterCapacity(c) != instance.getClusterSize(c)) {
                result.addFailure("Capacity constraint still relaxed for cluster " + c);
            }
            double pairSum = 0;
            for (int p : cluster) {
                membership[p]++;
                if (solution.clusterOf(p) != c) result.addFailure("Incorrect cluster lookup for point " + p);
                for (int q : cluster) {
                    if (p < q) pairSum += instance.distance(p, q);
                }
            }
            checkValue(result, "Pair sum for cluster " + c, pairSum, solution.getPairSum(c));
            cost += contribution(pairSum, size);
            for (int p = 0; p < instance.n; p++) {
                double sum = 0;
                for (int q : cluster) sum += instance.distance(p, q);
                checkValue(result, "Distance cache for point " + p + ", cluster " + c,
                        sum, solution.getPointClusterDistance(p, c));
            }
        }
        for (int p = 0; p < instance.n; p++) {
            boolean unassigned = solution.getNotAssignedPoints().contains(p);
            if (membership[p] > 1 || (membership[p] == 0) != unassigned
                    || unassigned != (solution.clusterOf(p) == -1)) {
                result.addFailure("Inconsistent membership for point " + p);
            }
            if (complete && membership[p] != 1) result.addFailure("Point " + p + " must be assigned exactly once");
        }
        checkValue(result, "Cost", cost, solution.getCost());
        return result;
    }

    private static void checkValue(ValidationResult result, String name, double expected, double actual) {
        if (!Double.isFinite(expected) || !Double.isFinite(actual) || !DoubleComparator.equals(expected, actual)) {
            result.addFailure(name + ": expected " + expected + ", got " + actual);
        }
    }
}
