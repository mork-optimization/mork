package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.util.DoubleComparator;

/**
 * Validate that a solution is valid for the TSPTW problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class TSPTWSolutionValidator extends SolutionValidator<TSPTWSolution, TSPTWInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(TSPTWSolution solution) {
        var instance = solution.getInstance();
        var tour = solution.permutation;
        int n = instance.n();
        if (tour.size() != n + 1 || tour.getFirst() != 0 || tour.getLast() != 0) {
            return ValidationResult.fail("Tour must visit every customer and start and finish at the depot");
        }

        var result = ValidationResult.ok();
        boolean[] visited = new boolean[n];
        visited[0] = true;
        double cost = 0;
        double arrival = 0;
        double infeasibility = 0;
        int violations = 0;
        int previous = 0;
        for (int i = 1; i <= n; i++) {
            int node = tour.get(i);
            if (node < 0 || node >= n || (i < n && visited[node])) {
                return ValidationResult.fail("Invalid or repeated customer at tour position " + i);
            }
            visited[node] = true;
            double distance = instance.dist(previous, node);
            cost += distance;
            arrival = Math.max(arrival + distance, instance.getWindowStart(node));
            if (!Double.isFinite(arrival) || !DoubleComparator.equals(arrival, solution._makespan[i])) {
                result.addFailure("Incorrect arrival-time cache at tour position " + i);
            }
            if (arrival > instance.getWindowEnd(node)) {
                violations++;
                infeasibility += arrival - instance.getWindowEnd(node);
            }
            previous = node;
        }

        if (violations > 0) {
            result.addFailure("Tour violates " + violations + " time windows");
        }
        if (violations != solution.constraint_violations()) {
            result.addFailure("Incorrect constraint-violation count");
        }
        if (!Double.isFinite(infeasibility) || !DoubleComparator.equals(infeasibility, solution.infeasibility())) {
            result.addFailure("Incorrect infeasibility cache");
        }
        if (!Double.isFinite(cost) || !DoubleComparator.equals(cost, solution.cost())) {
            result.addFailure("Incorrect tour-cost cache");
        }
        if (solution.nodes_available != 0) {
            result.addFailure("Complete tour has unassigned customers in its cache");
        }
        for (int node = 0; node < n; node++) {
            if (!visited[node] || !solution.node_assigned[node]) {
                result.addFailure("Missing customer or incorrect assignment cache: " + node);
            }
        }
        return result;
    }
}
