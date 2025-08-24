package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Example movement class. Can be an insert, a swap, anything that modifies the solution state
 */
public abstract class TSPTWBaseMove extends Move<TSPTWSolution, TSPTWInstance> {

    // common properties between moves should be stored here

    /**
     * Move constructor
     * @param solution solution
     */
    public TSPTWBaseMove(TSPTWSolution solution) {
        super(solution);
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type.
     * It is up to the implementation to decide if the original solution is modified
     * in place or a new one is created by cloning the original solution and then applying the changes.
     * <p></p>
     * This method should be idempotent, i.e. calling it multiple times with the same solution
     * should return the same result
     * @param solution Solution where this move will be applied to.
     * @return modified solution
     */
    @Override
    protected abstract TSPTWSolution _execute(TSPTWSolution solution);

    /**
     * Get the movement value, represents how much does the move changes the f.o of a solution if executed
     *
     * @return f.o change
     */
    public abstract double getScoreChange();

    /**
     * Returns a String representation of the current movement. Only use relevant fields.
     * Tip: Default IntelliJ implementation is fine
     *
     * @return human readable string
     */
    public abstract String toString();

    /** {@inheritDoc} */
    @Override
    public abstract boolean equals(Object o);

    /** {@inheritDoc} */
    @Override
    public abstract int hashCode();

}
