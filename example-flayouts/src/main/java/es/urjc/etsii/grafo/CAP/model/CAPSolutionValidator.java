package es.urjc.etsii.grafo.CAP.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Validate that a solution is valid for the CAP problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class CAPSolutionValidator extends SolutionValidator<CAPSolution, CAPInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(CAPSolution solution) {
        // Example check:

//        if(solution.getAssignedElements() > 10){
//            return ValidationResult.fail("Cannot have more than 10 assigned elements");
//        }
//        if(solution.myArray.length == 0){
//            return ValidationResult.fail("Empty array data, should have at least 5 elements assigned");
//        }

        return ValidationResult.ok();
    }
}
