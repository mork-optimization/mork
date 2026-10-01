package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.solution.Move;
import java.util.Objects;

/** Relocation (second is destination group) or atomic swap (second is another facility). */
public final class MREFLPMove extends Move<MREFLPSolution, MREFLPInstance> {
    private final int first, second;
    private final boolean swap;
    private final long delta;

    public MREFLPMove(MREFLPSolution solution, int first, int second, boolean swap, long delta) {
        super(solution);
        this.first = first;
        this.second = second;
        this.swap = swap;
        this.delta = delta;
    }

    public long delta() { return delta; }
    public int first() { return first; }
    public int second() { return second; }
    @Override protected MREFLPSolution _execute(MREFLPSolution solution) {
        if (swap) solution.swap(first, second, delta); else solution.relocate(first, second, delta);
        return solution;
    }
    @Override public boolean equals(Object o) {
        return o instanceof MREFLPMove m && first == m.first && second == m.second && swap == m.swap && delta == m.delta && solutionVersion == m.solutionVersion;
    }
    @Override public int hashCode() { return Objects.hash(first, second, swap, delta, solutionVersion); }
    @Override public String toString() { return (swap ? "Swap" : "OneMove") + "(" + first + "," + second + "," + delta + ")"; }
}
