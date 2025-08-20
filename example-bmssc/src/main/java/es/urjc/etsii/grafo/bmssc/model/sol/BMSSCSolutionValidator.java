package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

import java.util.Set;

/**
 * Validate that a solution is valid for the BMSSC problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class BMSSCSolutionValidator extends SolutionValidator<BMSSCSolution, BMSSCInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution BaseSolution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(BMSSCSolution solution) {
        var instance = solution.getInstance();

        Set<Integer>[] sets = solution.clusters;
        for (int i = 0; i < sets.length; i++) {
            Set<Integer> set = sets[i];
            if (set.size() != instance.getClusterSize(i)) {
                return ValidationResult.fail("Wrong cluster size for index %s, expected %s, got %s".formatted(i, set.size(), instance.getClusterSize(i)));
            }
        }

        return ValidationResult.ok();
    }
}
