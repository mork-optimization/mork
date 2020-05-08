package es.urjc.etsii.grafo.solution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.urjc.etsii.grafo.io.Instance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class Solution<I extends Instance> {

    static final int MAX_DEBUG_MOVES = 10;

    /**
     * Ignore field when serializing solutions to avoid data duplication
     */
    @JsonIgnore
    private final I ins;

    /**
     * Each time a move is executed es.urjc.etsii.grafo.solution version number must be incremented
     */
    long version = 0;

    protected ArrayDeque<Move<?, ?>> lastMoves = new ArrayDeque<>(MAX_DEBUG_MOVES);

    private long executionTimeInNanos;

    public Solution(I ins) {
        this.ins = ins;
    }

    /**
     * Validate current es.urjc.etsii.grafo.solution state
     * You must check that no constraints are broken, and that all costs are valid
     *
     * @return True if the es.urjc.etsii.grafo.solution is in a valid state, false otherwise
     */
    public abstract boolean isValid();

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

    // TODO que pasa si la solucion es multiobjetivo?
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

}
