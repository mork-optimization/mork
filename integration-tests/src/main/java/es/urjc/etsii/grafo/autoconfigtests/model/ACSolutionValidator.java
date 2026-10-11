package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/** Minimal validation for the synthetic integration-test objective. */
public class ACSolutionValidator extends SolutionValidator<ACSolution, ACInstance> {
    @Override public ValidationResult validate(ACSolution solution) {
        return solution != null && Double.isFinite(solution.getScore()) ? ValidationResult.ok() : ValidationResult.fail("Missing or nonfinite score");
    }
}
