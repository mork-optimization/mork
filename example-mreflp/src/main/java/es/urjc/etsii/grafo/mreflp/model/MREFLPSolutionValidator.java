package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

public class MREFLPSolutionValidator extends SolutionValidator<MREFLPSolution, MREFLPInstance> {
    @Override public ValidationResult validate(MREFLPSolution solution) {
        var instance = solution.getInstance();
        int[] counts = new int[instance.groups()];
        for (int v = 0; v < instance.n(); v++) {
            int g = solution.group(v);
            if (g < 0 || g >= counts.length) return ValidationResult.fail("Missing or invalid assignment for facility " + v);
            counts[g]++;
        }
        for (int g = 0; g < counts.length; g++) {
            if (counts[g] > instance.capacity() || counts[g] != solution.occupancy(g)) return ValidationResult.fail("Invalid occupancy in group " + g);
        }
        return solution.cost() == solution.recalculateCost() ? ValidationResult.ok() : ValidationResult.fail("Cached cost mismatch");
    }
}
