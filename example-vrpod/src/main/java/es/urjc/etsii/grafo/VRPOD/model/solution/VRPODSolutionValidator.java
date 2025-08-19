package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;

/**
 * Validate that a solution is valid for the VRPOD problem.
 * Validation is always run after the algorithms executes, and can be run in certain algorithm stages to verify
 * that the current solution is valid.
 */
public class VRPODSolutionValidator extends SolutionValidator<VRPODSolution, VRPODInstance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    @Override
    public ValidationResult validate(VRPODSolution solution) {

        for(var r: solution.normalDrivers){
            if(r == null) continue;
            String result = solution.isValid(r);
            if(!result.isBlank()){
                return ValidationResult.fail(result);
            }
        }

        return ValidationResult.ok();
    }
}
