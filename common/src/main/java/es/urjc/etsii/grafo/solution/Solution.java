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
    @JsonIgnore
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
        notifyUpdate(System.nanoTime());
    }

    /**
     * <p>notifyUpdate.</p>
     */
    public void notifyUpdate(long when) {
        this.lastModifiedTime = when;
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
     * Generate a string representation of the chain of moves used to reach the current solution state.
     * @return string representation, each move is separated by a new line
     */
    public String lastExecutesMovesAsString(){
        var sb = new StringBuilder();
        for (var move : lastExecutesMoves()) {
            String string = move.toString();
            sb.append('\n').append(string);
        }
        return sb.toString();
    }

    /**
     * Clone the current solution.
     * Deep clone mutable data or you will regret it.
     *
     * @return A deep clone of the current solution
     */
    public abstract SELF cloneSolution();

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
