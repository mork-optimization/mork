package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;

/**
 * Solution validator: If implemented, validates the generated solutions from different algorithms
 * during the execution, in order to detect bugs as soon as possible.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class SolutionValidator<S extends Solution<S,I>, I extends Instance> {

    /**
     * Validate the current solution, check that no constraint is broken and everything is fine
     *
     * @param solution Solution to validate
     * @return ValidationResult.ok() if the solution is valid, ValidationResult.fail("reason why it failed") if a solution is not valid.
     */
    public abstract ValidationResult validate(S solution);
}
