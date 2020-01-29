package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Comparator;

/**
 * All neighborhood moves should be represented by different instances of this class.
 * As they are in the same package as the Solution, they can efficiently manipulate it
 */
public abstract class Move<S extends Solution<I>, I extends Instance> {

    protected final long solutionVersion;

    public static final class MoveComparator<S extends Solution<I>, I extends Instance> implements Comparator<Move<S,I>> {
        @Override
        public int compare(Move<S, I> a, Move<S, I> b) {
            boolean bestA = a.getBestMove(b) == a;
            boolean bestB = b.getBestMove(a) == b;
            assert bestA || bestB;
            if(bestA && bestB)  return 0;
            if(bestA)           return -1;
            else                return 1;
        }
    }

    protected S s;

    public Move(S s) {
        this.s = s;
        this.solutionVersion = s.version;
    }

    /**
     * Does the es.urjc.etsii.grafo.solution improve if the current move is applied
     * @return True if the es.urjc.etsii.grafo.solution improves, false otherwise
     */
    public abstract boolean improves();

    /**
     * Get the best move between two candidates
     * @param o The other move
     * @return Returns the best move
     */
    public abstract Move<S,I> getBestMove(Move<S,I> o);

    /**
     * Executes the proposed move
     */
    public final void execute(){
        if(this.solutionVersion != s.version){
            throw new AssertionError(String.format("Solution state changed (%s), cannot execute move (%s)", s.version, this.solutionVersion));
        }
        _execute();
        s.version++;
        // TODO validate es.urjc.etsii.grafo.solution state after each move is applied
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
    protected abstract Move<S,I> next();
}
