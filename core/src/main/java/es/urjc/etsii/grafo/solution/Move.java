package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solver.Config;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.logging.Logger;

import static es.urjc.etsii.grafo.solution.Solution.MAX_DEBUG_MOVES;

/**
 * All neighborhood moves should be represented by different instances of this class.
 * As they are in the same package as the Solution, they can efficiently manipulate it
 */
public abstract class Move<S extends Solution<I>, I extends Instance> {

    private static final Logger logger = Logger.getLogger(Move.class.getName());

    protected final long solutionVersion;

    protected S s;

    public Move(S s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Is the solution in a valid state after this movement is applied?
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
        ValidationUtil.validSolution(s);
    }

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
    public boolean improves() {
        if (Config.isMaximizing()) {
            return DoubleComparator.isPositive(this.getValue());
        } else {
            return DoubleComparator.isNegative(this.getValue());
        }
    }

    /**
     * Get next move in this sequence.
     *
     * @return the next move in this generator sequence if there is a next move, null otherwise
     */
    public abstract Move<S, I> next();

    /**
     * Returns an String representation of the current movement.
     * Tip: Default IDEs implementations are usually fine
     *
     * @return human readable string
     */
    public abstract String toString();

    @Override
    public abstract boolean equals(Object o);

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
