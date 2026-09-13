package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import java.util.Optional;

/** Type boundary for neighborhoods that preserve the assigned facility set. */
public abstract class FLPPreservingNeighNew extends RandomizableNeighborhood<FLPNewMove, FLPSolution, FLPInstance> {
    protected final boolean fast;
    protected final boolean lazy;
    protected FLPPreservingNeighNew(boolean fast, boolean lazy) { this.fast = fast; this.lazy = lazy; }
    protected abstract FLPNewUtil.MoveSpace space(FLPSolution solution);
    @Override
    public ExploreResult<FLPNewMove, FLPSolution, FLPInstance> explore(FLPSolution solution) {
        if (es.urjc.etsii.grafo.util.TimeControl.isTimeUp()) return ExploreResult.empty();
        return FLPNewUtil.explore(space(solution), lazy);
    }
    @Override
    public Optional<FLPNewMove> getRandomMove(FLPSolution solution) {
        if (es.urjc.etsii.grafo.util.TimeControl.isTimeUp()) return Optional.empty();
        return FLPNewUtil.randomMove(space(solution));
    }
    @Override
    public int neighborhoodSize(FLPSolution solution) {
        long n = solution.nAssigned();
        return (int) Math.min(Integer.MAX_VALUE - 1L, n * (n + solution.nRows()));
    }
}
