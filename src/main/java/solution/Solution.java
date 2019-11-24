package solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.Instance;

public abstract class Solution {

    /**
     * Ignore field when serializing solutions to avoid data duplication
     */
    @JsonIgnore
    private final Instance ins;

    /**
     * Each time a movement is executed solution version number must be incremented
     */
    long version = 0;

    private long executionTimeInNanos;

    public Solution(Instance ins) {
        this.ins = ins;
    }

    /**
     * Validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * @return True if the solution is in a valid state, false otherwise
     */
    public abstract boolean isValid();

    /**
     * Clone the current solution.
     * Deep clone mutable data or you will regret it.
     * @return A deep clone of the current solution
     */
    public abstract Solution clone();

    /**
     * Compare current solution against another. Depending on the problem type (minimiz, max, multiobject)
     * the comparison will be different
     * @param o Solution to compare
     * @return Best solution
     */
    public abstract Solution getBetterSolution(Solution o);

    // TODO que pasa si la solucion es multiobjetivo?
    public abstract double getOptimalValue();

    /**
     * Resume this solution
     * Generate a toString method using your IDE
     * @return
     */
    public abstract String toString();

    public Instance getIns() {
        return ins;
    }

    public void setExecutionTimeInNanos(long executionTimeInNanos) {
        this.executionTimeInNanos = executionTimeInNanos;
    }

    public long getExecutionTimeInNanos() {
        return executionTimeInNanos;
    }
}
