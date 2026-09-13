package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.RefreshableMove;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Atomic, refreshable move used by the New operators. Destinations are identified by the
 * facility to insert before, or by a row when appending. Block members retain their order.
 * Evaluation never mutates the source solution; execution publishes exactly one update.
 */
public final class FLPNewMove extends FLPMove implements RefreshableMove<FLPNewMove, FLPSolution, FLPInstance> {
    public enum Kind { ADD, REMOVE, SWAP, REVERSE, RELOCATE }

    final Kind kind;
    final int[] facilities;
    final int row;
    final int anchor;
    final boolean reverse;
    final boolean fast;
    private final FLPInstance instance;

    FLPNewMove(FLPSolution solution, Kind kind, int[] facilities, int row, int anchor, boolean reverse, boolean fast) {
        super(solution, FLPNewUtil.delta(solution, kind, facilities, row, anchor, reverse, fast));
        this.kind = kind;
        this.facilities = facilities.clone();
        this.row = row;
        this.anchor = anchor;
        this.reverse = reverse;
        this.fast = fast;
        this.instance = solution.getInstance();
    }

    @Override
    protected FLPSolution _execute(FLPSolution solution) {
        var layout = FLPNewUtil.changedRows(solution, kind, facilities, row, anchor, reverse);
        FLPNewUtil.install(solution, layout, solution.getScore() + delta);
        return solution;
    }

    @Override
    public Optional<FLPNewMove> refresh(FLPSolution solution) {
        if (solution.getInstance() != instance) return Optional.empty();
        try {
            return Optional.of(new FLPNewMove(solution, kind, facilities, row, anchor, reverse, fast));
        } catch (IllegalArgumentException e) {
            // A member/anchor was removed, or a block is no longer contiguous.
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FLPNewMove m)) return false;
        return kind == m.kind && row == m.row && anchor == m.anchor && reverse == m.reverse
                && Arrays.equals(facilities, m.facilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, Arrays.hashCode(facilities), row, anchor, reverse);
    }

    @Override
    public String toString() {
        return kind + Arrays.toString(facilities) + " row=" + row + " before=" + anchor + " reverse=" + reverse + " delta=" + delta;
    }
}
