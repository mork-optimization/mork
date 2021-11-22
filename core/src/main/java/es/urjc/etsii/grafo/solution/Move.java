package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(Move.class.getName());

    /**
     * This variable is used internally to check if the current solution has been modified.
     */
    protected final long solutionVersion;

    /**
     * Solution on which the movements are performed.
     */
    protected S s;

    /**
     * Move constructor
     *
     * @param s solution
     */
    public Move(S s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Method that checks if the solution is in a valid state after applying a move.
     *
     * @return True if the solution is in a valid state after applying the movement, false if we break any constraint
     */
    public abstract boolean isValid();

    /**
     * Executes the proposed move
     */
    public final void execute() {
        logger.finer(this.toString());
        if (this.solutionVersion != s.version) {
            throw new AssertionError(String.format("Solution state changed to (%s), cannot execute move referencing solution state (%s)", s.version, this.solutionVersion));
        }
        assert saveLastMove(this);
        double prevScore = s.getScore();
        _execute();
        if (!DoubleComparator.equals(prevScore, s.getScore())) {
            // Some moves may not affect the optimal score
            s.updateLastModifiedTime();
        }
        s.version++;
        ValidationUtil.assertValidScore(s);
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
    private boolean saveLastMove(Move<S, I> move) {
        if (this.s.lastMoves.size() >= MAX_DEBUG_MOVES) {
            this.s.lastMoves.removeFirst();
        }
        this.s.lastMoves.add(move);
        return true;
    }

    /**
     * Executes the proposed move,
     * to be implemented by each move type
     */
    protected abstract void _execute();

    /**
     * Get the movement value, represents how much does the move changes the f.o of a solution if executed
     *
     * @return f.o change
     */
    public abstract double getValue();

    /**
     * Does the solution improve if the current move is applied?
     *
     * @return True if solution improves, false otherwise
     */
    public abstract boolean improves();

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

    /**
     * Get the solution this move originated from
     *
     * @return Solution
     */
    public S getSolution() {
        return s;
    }
}
