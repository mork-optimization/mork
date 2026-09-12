package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.RefreshableMove;

import java.util.Objects;
import java.util.Optional;

public final class SwapMove extends BMSSCMove implements RefreshableMove<SwapMove, BMSSCSolution, BMSSCInstance> {
    private final int p;
    private final int q;
    private final double delta;

    public SwapMove(BMSSCSolution solution, int p, int q) {
        super(solution);
        if (!solution.canSwap(p, q)) throw new IllegalArgumentException("Invalid swap");
        this.p = Math.min(p, q);
        this.q = Math.max(p, q);
        this.delta = solution.swapDelta(this.p, this.q);
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        solution.swap(p, q);
        return solution;
    }

    @Override
    public Optional<SwapMove> refresh(BMSSCSolution solution) {
        return solution.canSwap(p, q) ? Optional.of(new SwapMove(solution, p, q)) : Optional.empty();
    }

    @Override
    public double getCostDelta() { return delta; }
    public int getFirstPoint() { return p; }
    public int getSecondPoint() { return q; }

    @Override
    public String toString() { return "Swap " + p + " <-> " + q + ", delta " + delta; }

    @Override
    public boolean equals(Object o) {
        return o instanceof SwapMove other && p == other.p && q == other.q;
    }

    @Override
    public int hashCode() { return Objects.hash(p, q); }
}
