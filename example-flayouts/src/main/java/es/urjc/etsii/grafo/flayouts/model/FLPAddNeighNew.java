package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import java.util.List;
import java.util.Optional;

/** Corrected insertion variant of FLPAddNeigh; evaluation leaves all solution state untouched. */
public class FLPAddNeighNew extends RandomizableNeighborhood<FLPNewMove, FLPSolution, FLPInstance> {
    protected final boolean fast;
    protected final boolean lazy;
    @AutoconfigConstructor
    public FLPAddNeighNew() { this(false, false); }
    protected FLPAddNeighNew(boolean fast, boolean lazy) { this.fast = fast; this.lazy = lazy; }

    private FLPNewUtil.MoveSpace space(FLPSolution s) {
        var missing = FLPNewUtil.missing(s);
        var gaps = FLPNewUtil.gaps(s);
        return new FLPNewUtil.MoveSpace((long) missing.size() * gaps.length, index -> {
            int f = missing.get((int) (index / gaps.length));
            var gap = gaps[(int) (index % gaps.length)];
            return FLPNewUtil.add(s, f, gap.row(), gap.pos(), fast);
        });
    }
    @Override
    public ExploreResult<FLPNewMove, FLPSolution, FLPInstance> explore(FLPSolution s) {
        if (es.urjc.etsii.grafo.util.TimeControl.isTimeUp()) return ExploreResult.empty();
        return FLPNewUtil.explore(space(s), lazy);
    }
    public List<FLPNewMove> exploreList(FLPSolution s) {
        try (var moves = explore(s).moves()) { return moves.toList(); }
    }
    @Override
    public Optional<FLPNewMove> getRandomMove(FLPSolution s) { return FLPNewUtil.randomMove(space(s)); }
    @Override
    public int neighborhoodSize(FLPSolution s) {
        return (int) Math.min(Integer.MAX_VALUE - 1L, (long) s.getNotAssignedFacilities().size() * (s.nAssigned() + s.nRows()));
    }
}
