package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Objects;

import static es.urjc.etsii.grafo.solution.Solution.MAX_DEBUG_MOVES;

/**
 * All neighborhood moves should be represented by different instances of this class.
 * As they are in the same package as the Solution, they can efficiently manipulate it
 */
public abstract class Move<S extends Solution<I>, I extends Instance> {

    protected final long solutionVersion;

    protected S s;

    public Move(S s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Does the solution improve if the current move is applied?
     * @return True if solution improves, false otherwise
     */
    public abstract boolean improves();

    /**
     * Is the solution in a valid state after this movement is applied?
     * @return True if the solution is in a valid state after applying the movement, false otherwise
     */
    public abstract boolean isValid();

    /**
     * Executes the proposed move
     */
    public final void execute(){
        if(this.solutionVersion != s.version){
            throw new AssertionError(String.format("Solution state changed (%s), cannot execute move (%s)", s.version, this.solutionVersion));
        }
        assert saveLastMoves(this);
        double prevScore = s.getOptimalValue();
        _execute();
        if (!DoubleComparator.equals(prevScore, s.getOptimalValue())) {
            // Some moves may not affect the optimal score
            s.updateLastModifiedTime();
        }
        s.version++;
        assert s.isValid();
    }

    private boolean saveLastMoves(Move<?,?> move){
        if(this.s.lastMoves.size()>=MAX_DEBUG_MOVES){
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
     * Get the movement value, represents how much does the move affect the es.urjc.etsii.grafo.solution if executed
     * @return
     */
    public abstract double getValue();

    /**
     * Get next move in this sequence.
     * @return the next move in this generator sequence if there is a next move, null otherwise
     */
    public abstract Move<S,I> next();

    public abstract String toString();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public int hashCode() {
        return Objects.hash(solutionVersion, s);
    }

    /**
     * Get the solution this move originated from
     * @return Solution
     */
    public S getSolution() {
        return s;
    }
}
