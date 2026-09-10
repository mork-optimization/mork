package es.urjc.etsii.grafo.mmdp.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Checks that a MMDP solution is consistent: the cached score must match a from-scratch
 * recalculation. Run automatically after each (instance, algorithm, repetition).
 */
public class MMDPSolutionValidator extends SolutionValidator<MMDPSolution, MMDPInstance> {

    @Override
    public ValidationResult validate(MMDPSolution solution) {
        var result = ValidationResult.ok();

        double recalculated = solution.recalculateScore();
        if (Math.abs(recalculated - solution.getScore()) > 1e-6) {
            result.addFailure("Score mismatch: cached " + solution.getScore()
                    + " but recalculated " + recalculated);
        }

        return result;
    }
}
