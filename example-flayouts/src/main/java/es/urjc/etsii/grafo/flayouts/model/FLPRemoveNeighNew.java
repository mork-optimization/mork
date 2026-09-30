package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import java.util.Optional;

/** Corrected removal variant; keeps counters, missing facilities, centers and objective consistent. */
public class FLPRemoveNeighNew extends RandomizableNeighborhood<FLPNewMove, FLPSolution, FLPInstance> {
    private final boolean fast;

    @AutoconfigConstructor
    public FLPRemoveNeighNew() { this(true); }

    FLPRemoveNeighNew(boolean fast) { this.fast = fast; }
    public FLPNewMove removeAll(FLPSolution s, int... facilities) {
        int[] ids = facilities.clone();
        java.util.Arrays.sort(ids);
        return new FLPNewMove(s, FLPNewMove.Kind.REMOVE, ids, -1, -1, false, fast);
    }
    private FLPNewUtil.MoveSpace<FLPNewMove> space(FLPSolution s) {
        var positions = FLPNewUtil.positions(s);
        return new FLPNewUtil.MoveSpace<>(positions.length, i -> {
            var p = positions[(int) i];
            return removeAll(s, s.rows[p.row()][p.pos()]);
        });
    }
    @Override
    public ExploreResult<FLPNewMove, FLPSolution, FLPInstance> explore(FLPSolution s) { return FLPNewUtil.explore(space(s), fast); }
    @Override
    public Optional<FLPNewMove> getRandomMove(FLPSolution s) { return FLPNewUtil.randomMove(space(s)); }
    @Override
    public int neighborhoodSize(FLPSolution s) { return s.nAssigned(); }
}
