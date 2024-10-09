package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Example movement class. Can be an insert, a swap, anything that modifies the solution state
 */
public abstract class __RNAME__BaseMove extends Move<__RNAME__Solution, __RNAME__Instance> {

    // common properties between moves should be stored here

    /**
     * Move constructor
     * @param solution solution
     */
    public __RNAME__BaseMove(__RNAME__Solution solution) {
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
    protected abstract __RNAME__Solution _execute(__RNAME__Solution solution);

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
