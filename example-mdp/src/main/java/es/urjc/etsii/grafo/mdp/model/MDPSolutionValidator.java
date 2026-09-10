package es.urjc.etsii.grafo.mdp.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Checks that an MDP solution is feasible: it must select exactly {@code m} nodes, and its cached
 * diversity must match a from-scratch recalculation. Run automatically after each
 * (instance, algorithm, repetition). Reused unchanged from {@code mork-experiments}.
 */
public class MDPSolutionValidator extends SolutionValidator<MDPSolution, MDPInstance> {

    @Override
    public ValidationResult validate(MDPSolution solution) {
        var result = ValidationResult.ok();

        int expected = solution.getInstance().getNumSolutionNodes();
        if (solution.getNumNodes() != expected) {
            result.addFailure("Solution must select " + expected + " nodes but has " + solution.getNumNodes());
        }

        double recalculated = solution.recalculateScore();
        double tolerance = 1e-6 * Math.max(1.0, Math.abs(recalculated));
        if (Math.abs(recalculated - solution.getWeight()) > tolerance) {
            result.addFailure("Score mismatch: cached " + solution.getWeight()
                    + " but recalculated " + recalculated);
        }

        return result;
    }
}
