package es.urjc.etsii.grafo.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.urjc.etsii.grafo.io.Instance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>Abstract Solution class.</p>
 *
 */
public abstract class Solution<SELF extends Solution<SELF, I>, I extends Instance> {

    static final int MAX_DEBUG_MOVES = 100;

    /**
     * Ignore field when serializing solutions to avoid data duplication
     */
    private final I ins;

    /**
     * Each time a move is executed solution version number must be incremented
     */
    long version = 0;

    protected ArrayDeque<Move<? extends Solution<SELF, I>, I>> lastMoves = new ArrayDeque<>(MAX_DEBUG_MOVES);

    protected long lastModifiedTime = Integer.MIN_VALUE;

    /**
     * Create a solution for a given instance
     *
     * @param ins Instance
     */
    protected Solution(I ins) {
        this.ins = ins;
    }

    /**
     * <p>Constructor for Solution.</p>
     *
     * @param s a {@link Solution} object.
     */
    public Solution(Solution<SELF, I> s){
        // Only copy lastMoves when debugging
        assert (this.lastMoves = new ArrayDeque<>(s.lastMoves)) != null;
        this.ins = s.ins;
        this.version = s.version;
        this.lastModifiedTime = s.lastModifiedTime;
    }


    /**
     * <p>notifyUpdate.</p>
     */
    public void notifyUpdate() {
        this.lastModifiedTime = System.nanoTime();
    }

    /**
     * Returns ordered list of oldest to recent moves
     * Note: If assertions are disabled, always returns an empty list
     *
     * @return ordered list of oldest to recent moves
     */
    public List<Move<? extends Solution<SELF, I>, I>> lastExecutesMoves(){
        return new ArrayList<>(this.lastMoves);
    }

    /**
     * Clone the current solution.
     * Deep clone mutable data or you will regret it.
     *
     * @return A deep clone of the current solution
     */
    public abstract SELF cloneSolution();

    /**
     * Check if this solution is STRICTLY better than the given solution.
     * If they are equivalent this method must return false.
     *
     * @param other a different solution to compare
     * @return true if and only if the current solution is strictly better than the given solution
     */
    public boolean isBetterThan(SELF other){
        if(other == null){
            return true;
        }
        if(this == other){
            return false;
        }
        return _isBetterThan(other);
    }

    /**
     * Check if this solution is STRICTLY better than the given solution.
     * If they are equivalent this method must return false.
     * This method must be implemented by all subclasses
     *
     * @param other a different solution to compare
     * @return true if and only if the current solution is strictly better than the given solution
     */
    protected abstract boolean _isBetterThan(SELF other);

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    public abstract double getScore();

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
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

    /**
     * <p>getInstance.</p>
     *
     * @return a I object.
     */
    @JsonIgnore
    public I getInstance() {
        return ins;
    }

    /**
     * <p>getBest.</p>
     *
     * @param solutions a {@link Iterable} object.
     * @param <I> a I object.
     * @param <S> a S object.
     * @return a S object.
     */
    public static <I extends Instance, S extends Solution<S, I>> S getBest(Iterable<S> solutions) {
        S best = null;
        for (S solution : solutions) {
            if(solution.isBetterThan(best)){
                best = solution;
            }
        }
        return best;
    }

    /**
     * When was the last time the current solution was modified.
     * Has no meaning when used independently, must be used along a reference time (for example, to calculate the TTB, substracting the time at which the current solution was built)
     * @return reference time, in nano seconds.
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Get current solution version. 
     * This is an internal ID kept by the framework, which tracks how many times the current solution has been modified.
     * While it currently equals the number of moves that has been applied to the current solution, 
     * such behaviour can change in future framework versions and should not be relied on.
     *
     * @return current solution version.
     */
    public long getVersion() {
        return this.version;
    }

    /**
     * Define custom properties for the solution
     * @return Map of properties, where the key is the property name and the value is how to calculate the property value
     */
    public Map<String, Function<SELF, Object>> customProperties(){
        return Map.of();
    }
}
