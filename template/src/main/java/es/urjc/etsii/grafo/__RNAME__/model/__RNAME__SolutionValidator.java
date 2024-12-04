package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Validate that a solution is valid for the __RNAME__ problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class __RNAME__SolutionValidator extends SolutionValidator<__RNAME__Solution, __RNAME__Instance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(__RNAME__Solution solution) {
        // You should implement this method to check that the solution is valid, without using any kind of existing caches or scores.
        // For example, you can recalculate solution score and check if it matches the score stored in the solution.
        var validationResult = ValidationResult.ok();
        // Example checks:

//        if(solution.getAssignedElements() > 10){
//            validationResult.addFailure("Cannot have more than 10 assigned elements");
//        }

//        double recalculateScore = {......};
//        if(solution.getScore() != recalculateScore){
//            validationResult.addFailure("Score mismatch, expected: " + recalculateScore + ", got: " + solution.getScore());
//        }

//        if(!solution.unassignedClients.isEmpty()){
//            validationResult.addFailure("Invalid solution, all clients should be assigned. Remaining clients: " + solution.unassignedClients);
//        }

        return validationResult;
    }
}
