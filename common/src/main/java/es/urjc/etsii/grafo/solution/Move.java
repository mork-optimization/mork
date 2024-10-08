package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static es.urjc.etsii.grafo.solution.Solution.MAX_DEBUG_MOVES;

/**
 * Represents a change for a given solution in a given neighborhood
 */
public abstract class Move<S extends Solution<S, I>, I extends Instance> {

    /**
     * Static object to generate log messages.
     */
    private static final Logger logger = LoggerFactory.getLogger(Move.class);

    private static boolean asserts = false;
    static {
        // Intentional side effect to check if assertions are enabled for this class
        //noinspection AssertWithSideEffects
        assert asserts = true;
    }

    /**
     * This variable is used internally to check if the current solution has been modified.
     */
    protected final long solutionVersion;

    /**
     * Move constructor
     *
     * @param solution solution
     */
    public Move(S solution) {
        this.solutionVersion = solution.version;
    }

    /**
     * Executes the proposed move
     */
    public final S execute(S solution) {
        if(logger.isTraceEnabled()){
            logger.trace(this.toString());
        }
        if (this.solutionVersion != solution.version) {
            throw new AssertionError(String.format("Solution state changed to (%s), cannot execute move referencing solution state (%s)", solution.version, this.solutionVersion));
        }
        if(asserts){
            return executeAsserts(solution);
        }
        return executeDirect(solution);
    }

    private S executeDirect(S solution){
        S newSolution = _execute(solution);
        solution.notifyUpdate();
        solution.version++;
        return newSolution;
    }

    private S executeAsserts(S solution){
        var oldValues = Context.evalSolution(solution);
        assert saveLastMove(solution, this);
        S newSolution = executeDirect(solution);
        assert ValidationUtil.scoreUpdate(solution, oldValues, this);
        assert Context.validate(solution);
        return newSolution;
    }

    /**
     * Save this movement in the list of movements performed in the solution.
     * By default, the maximum value of saved moves is 100 {@see Solution}
     * <p>
     * This function is only called in the debug mode (asserts active),
     * and allows the user to view the record of movements performed.
     *
     * @param move move
     * @return the value is always true to prevent producing a java.lang.AssertionError exception.
     */
    private boolean saveLastMove(S solution, Move<S, I> move) {
        if (solution.lastMoves.size() >= MAX_DEBUG_MOVES) {
            solution.lastMoves.removeFirst();
        }
        solution.lastMoves.add(move);
        return true;
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type
     */
    protected abstract S _execute(S solution);

    /**
     * Returns a String representation of the current movement.
     * Tip: Default IDEs implementations are usually fine
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
