package es.urjc.etsii.grafo.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.stopping.DummyStop;
import es.urjc.etsii.grafo.solution.stopping.StopPoint;

import java.util.ArrayDeque;

public abstract class Solution<I extends Instance> implements Comparable<Solution<I>> {

    static final int MAX_DEBUG_MOVES = 10;

    /**
     * Ignore field when serializing solutions to avoid data duplication
     */
    @JsonIgnore
    private final I ins;

    /**
     * Dummy stop by default, can be changed as the user wants
     */
    @JsonIgnore
    protected StopPoint stopPoint;

    /**
     * Each time a move is executed es.urjc.etsii.grafo.solution version number must be incremented
     */
    long version = 0;

    protected ArrayDeque<Move<?, ?>> lastMoves = new ArrayDeque<>(MAX_DEBUG_MOVES);

    private long executionTimeInNanos;

    private long lastModifiedTime;

    /**
     * Create a solution for a given instance using a custom StopPoint
     * @param ins
     * @param stopPoint
     */
    public Solution(I ins, StopPoint stopPoint) {
        this.ins = ins;
        this.stopPoint = stopPoint;
        if(!stopPoint.isStarted()){
            this.stopPoint.start();
        }
    }

    /**
     * Create a solution for a given instance.
     * @param ins
     */
    public Solution(I ins){
        this(ins, new DummyStop());
    }

    /**
     * Validate current es.urjc.etsii.grafo.solution state
     * You must check that no constraints are broken, and that all costs are valid
     *
     * @return True if the es.urjc.etsii.grafo.solution is in a valid state, false otherwise
     */
    public abstract boolean isValid();

    public void updateLastModifiedTime() {
        this.lastModifiedTime = System.nanoTime();
    }

    /**
     * Clone the current es.urjc.etsii.grafo.solution.
     * Deep clone mutable data or you will regret it.
     *
     * @return A deep clone of the current es.urjc.etsii.grafo.solution
     */
    public abstract <S extends Solution<I>> S cloneSolution();

    /**
     * Compare current es.urjc.etsii.grafo.solution against another. Depending on the problem type (minimiz, max, multiobject)
     * the comparison will be different
     *
     * @param o Solution to compare
     * @return Best es.urjc.etsii.grafo.solution
     */
    public abstract <S extends Solution<I>> S getBetterSolution(S o);

    public abstract double getOptimalValue();

    /**
     * Resume this es.urjc.etsii.grafo.solution
     * Generate a toString method using your IDE
     *
     * @return string representation of the current es.urjc.etsii.grafo.solution
     */
    public abstract String toString();

    public I getInstance() {
        return ins;
    }

    public void setExecutionTimeInNanos(long executionTimeInNanos) {
        this.executionTimeInNanos = executionTimeInNanos;
    }

    public long getExecutionTimeInNanos() {
        return executionTimeInNanos;
    }

    public boolean stop(){
        return this.stopPoint.stop();
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

    protected void copyInternalData(Solution<I> s){
        // Only copy lastMoves when debugging
        assert (this.lastMoves = new ArrayDeque<>(s.lastMoves)) != null;
        this.version = s.version;
        this.executionTimeInNanos = s.executionTimeInNanos;
        this.lastModifiedTime = s.lastModifiedTime;
        this.stopPoint = s.stopPoint;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Note, this compareTo implementation imposes orderings that are inconsistent with equals.
     * @param s Solution to compare against
     * @return -1, zero, or +1 if the current solution is better, equal or worse than the argument.
     */
    @Override
    public int compareTo(Solution<I> s) {
        boolean bestA = this.getBetterSolution(s) == this;
        boolean bestB = s.getBetterSolution(this) == s;

        assert bestA || bestB;
        if(bestA && bestB)  return 0;
        if(bestA)           return -1; // Best solutions go first
        else                return 1;
    }
}
