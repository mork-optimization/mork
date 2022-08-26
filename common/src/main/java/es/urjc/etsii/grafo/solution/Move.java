package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static es.urjc.etsii.grafo.solution.Solution.MAX_DEBUG_MOVES;

/**
 * Represents a change for a given solution in a given neighborhood
 * All neighborhood moves should be represented by implementations of either LazyMove or EagerMove.
 * Do not directly extend this class.
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
    public final boolean execute(S solution) {
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

    private boolean executeDirect(S solution){
        boolean changed = _execute(solution);
        if (changed) {
            // Some moves may not affect the optimal score
            solution.updateLastModifiedTime();
        }
        solution.version++;
        return changed;
    }

    private boolean executeAsserts(S solution){
        assert saveLastMove(solution, this);
        double prevScore = solution.getScore();
        boolean changed = executeDirect(solution);
        assert changed || DoubleComparator.equals(prevScore, solution.getScore()) :
                String.format("Solution score changed but execute() returned false, from %s to %s after applying move %s", prevScore, solution.getScore(), this);
        assert ValidationUtil.scoreUpdate(solution, prevScore, this);
        ValidationUtil.assertValidScore(solution);
        return changed;
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
    protected abstract boolean _execute(S solution);

    /**
     * Get the movement value, represents how much does the move changes the f.o of a solution if executed
     *
     * @return f.o change
     */
    public abstract double getValue();

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
