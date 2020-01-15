package solver.create;

import io.Instance;
import solution.Solution;

/**
 * Functional interface for generating empty solutions from a given instance.
 * Problem dependant, empty solution will then be filled by a constructor
 */
@FunctionalInterface
public interface SolutionBuilder {
    /**
     * Generate an empty solution with the parameters given by the user
     */
    <S extends Solution> S initializeSolution(Instance i);
}
