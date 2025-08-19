package es.urjc.etsii.grafo.CAP.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base class for all movements for the CAP problem. All movements should extend this class.
 */
public abstract class CAPBaseMove extends Move<CAPSolution, CAPInstance> {

    /**
     * Move constructor
     * @param solution solution
     */
    public CAPBaseMove(CAPSolution solution) {
        super(solution);
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type.
     * @param solution Solution where this move will be applied to.
     * @return true if the solution has changed,
     * false if for any reason the movement is not applied or the solution does not change after executing the move
     */
    @Override
    protected abstract CAPSolution _execute(CAPSolution solution);

    /**
     * Get the movement value, represents how much does the move changes the f.o of a solution if executed
     *
     * @return f.o change
     */
    public abstract double getValue();

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
