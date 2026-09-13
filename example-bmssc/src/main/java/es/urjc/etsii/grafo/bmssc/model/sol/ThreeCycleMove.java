package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.solution.RefreshableMove;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/** Rotate three sorted point IDs; reverse selects the opposite direction. */
public final class ThreeCycleMove extends BMSSCMove implements RefreshableMove<ThreeCycleMove, BMSSCSolution, BMSSCInstance> {
    private final int p, q, r;
    private final boolean reverse;
    private final double delta;

    public ThreeCycleMove(BMSSCSolution solution, int p, int q, int r, boolean reverse) {
        super(solution);
        int[] points = {p, q, r};
        Arrays.sort(points);
        this.p = points[0];
        this.q = points[1];
        this.r = points[2];
        this.reverse = reverse;
        if (!valid(solution)) throw new IllegalArgumentException("Cycle requires three distinct occupied clusters");
        int incomingP = reverse ? this.q : this.r;
        int incomingQ = reverse ? this.r : this.p;
        int incomingR = reverse ? this.p : this.q;
        delta = BMSSCNewUtil.replacementDelta(solution, solution.clusterOf(this.p), new int[]{this.p}, new int[]{incomingP})
                + BMSSCNewUtil.replacementDelta(solution, solution.clusterOf(this.q), new int[]{this.q}, new int[]{incomingQ})
                + BMSSCNewUtil.replacementDelta(solution, solution.clusterOf(this.r), new int[]{this.r}, new int[]{incomingR});
    }

    private boolean valid(BMSSCSolution solution) {
        return solution.canSwap(p, q) && solution.canSwap(p, r) && solution.canSwap(q, r);
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        // Low-level swaps update caches without publishing an intermediate objective or move version.
        if (reverse) {
            solution.swap(p, r);
            solution.swap(r, q);
        } else {
            solution.swap(p, q);
            solution.swap(q, r);
        }
        return solution;
    }

    @Override
    public Optional<ThreeCycleMove> refresh(BMSSCSolution solution) {
        return valid(solution) ? Optional.of(new ThreeCycleMove(solution, p, q, r, reverse)) : Optional.empty();
    }

    @Override
    public double getCostDelta() { return delta; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ThreeCycleMove other && p == other.p && q == other.q && r == other.r && reverse == other.reverse;
    }

    @Override
    public int hashCode() { return Objects.hash(p, q, r, reverse); }

    @Override
    public String toString() { return "ThreeCycle{" + p + "," + q + "," + r + ", reverse=" + reverse + ", delta=" + delta + "}"; }
}
