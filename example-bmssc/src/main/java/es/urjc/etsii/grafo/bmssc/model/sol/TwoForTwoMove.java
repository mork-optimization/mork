package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.solution.RefreshableMove;

import java.util.Arrays;
import java.util.Optional;

/** Exchange unordered pairs between two clusters as one scored move. */
public final class TwoForTwoMove extends BMSSCMove implements RefreshableMove<TwoForTwoMove, BMSSCSolution, BMSSCInstance> {
    private final int[] left, right;
    private final double delta;

    public TwoForTwoMove(BMSSCSolution solution, int p, int q, int r, int s) {
        super(solution);
        int[] first = {Math.min(p, q), Math.max(p, q)};
        int[] second = {Math.min(r, s), Math.max(r, s)};
        left = first[0] < second[0] ? first : second;
        right = first[0] < second[0] ? second : first;
        if (!valid(solution)) throw new IllegalArgumentException("Exchange requires two distinct points in each of two clusters");
        delta = BMSSCNewUtil.replacementDelta(solution, solution.clusterOf(left[0]), left, right)
                + BMSSCNewUtil.replacementDelta(solution, solution.clusterOf(right[0]), right, left);
    }

    private boolean valid(BMSSCSolution solution) {
        return left[0] != left[1] && right[0] != right[1]
                && solution.canSwap(left[0], right[0])
                && solution.clusterOf(left[0]) == solution.clusterOf(left[1])
                && solution.clusterOf(right[0]) == solution.clusterOf(right[1]);
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        solution.swap(left[0], right[0]);
        solution.swap(left[1], right[1]);
        return solution;
    }

    @Override
    public Optional<TwoForTwoMove> refresh(BMSSCSolution solution) {
        return valid(solution) ? Optional.of(new TwoForTwoMove(solution, left[0], left[1], right[0], right[1])) : Optional.empty();
    }

    @Override
    public double getCostDelta() { return delta; }

    @Override
    public boolean equals(Object o) {
        return o instanceof TwoForTwoMove other && Arrays.equals(left, other.left) && Arrays.equals(right, other.right);
    }

    @Override
    public int hashCode() { return 31 * Arrays.hashCode(left) + Arrays.hashCode(right); }

    @Override
    public String toString() { return "TwoForTwo{" + Arrays.toString(left) + " <-> " + Arrays.toString(right) + ", delta=" + delta + "}"; }
}
