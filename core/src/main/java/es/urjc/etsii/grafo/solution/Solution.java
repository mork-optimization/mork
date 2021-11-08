package es.urjc.etsii.grafo.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.urjc.etsii.grafo.io.Instance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public abstract class Solution<I extends Instance> {

    static final int MAX_DEBUG_MOVES = 100;

    /**
     * Ignore field when serializing solutions to avoid data duplication
     */
    private final I ins;

    /**
     * Each time a move is executed solution version number must be incremented
     */
    long version = 0;

    protected ArrayDeque<Move<? extends Solution<I>, I>> lastMoves = new ArrayDeque<>(MAX_DEBUG_MOVES);

    private long lastModifiedTime = Integer.MIN_VALUE;

    /**
     * Create a solution for a given instance
     * @param ins Instance
     */
    public Solution(I ins) {
        this.ins = ins;
    }

    public Solution(Solution<I> s){
        // Only copy lastMoves when debugging
        assert (this.lastMoves = new ArrayDeque<>(s.lastMoves)) != null;
        this.ins = s.ins;
        this.version = s.version;
        this.lastModifiedTime = s.lastModifiedTime;
    }


    public void updateLastModifiedTime() {
        this.lastModifiedTime = System.nanoTime();
    }

    /**
     * Returns ordered list of oldest to recent moves
     * Note: If assertions are disabled, always returns an empty list
     * @return ordered list of oldest to recent moves
     */
    public List<Move<? extends Solution<I>, I>> lastExecutesMoves(){
        return new ArrayList<>(this.lastMoves);
    }

    /**
     * Clone the current solution.
     * Deep clone mutable data or you will regret it.
     * @param <S> Solution class
     * @return A deep clone of the current solution
     */
    public abstract <S extends Solution<I>> S cloneSolution();

    /**
     * Compare current solution against another. Depending on the problem type (minimiz, max, multiobject)
     * the comparison will be different
     * @param <S> Solution class
     * @param o Solution to compare
     * @return Best solution
     */
    public abstract <S extends Solution<I>> S getBetterSolution(S o);

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     * @return current solution score as double
     */
    public abstract double getScore();

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * @return current solution score as double
     */
    public abstract double recalculateScore();

    /**
     * Resume this solution
     * Generate a toString method using your IDE
     *
     * @return string representation of the current solution
     */
    public abstract String toString();

    @JsonIgnore
    public I getInstance() {
        return ins;
    }

    public static <I extends Instance, S extends Solution<I>> S getBest(Iterable<S> solutions) {
        S best = null;
        for (S solution : solutions) {
            if (best == null) {
                best = solution;
            } else {
                best = best.getBetterSolution(solution);
            }
        }
        return best;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
}
