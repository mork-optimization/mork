package solver.create;

import solution.Solution;

/**
 * Fixes a partially assigned solution, so all elements must be assigned and no constraints are broken
 */
public interface SolutionFixer {
    /**
     * Fixes a partially assigned solution after a destructor iteration.
     * @param sol partially assigned solution
     * @return Valid solution, with no constraints broken and all elements assigned
     */
    Solution assignMissing(Solution sol);
}
