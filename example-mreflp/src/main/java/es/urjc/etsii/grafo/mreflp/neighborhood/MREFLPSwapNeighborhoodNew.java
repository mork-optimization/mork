package es.urjc.etsii.grafo.mreflp.neighborhood;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.neighborhood.*;
import java.util.Optional;

public final class MREFLPSwapNeighborhoodNew extends RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance> {
    @AutoconfigConstructor public MREFLPSwapNeighborhoodNew() {}
    @Override public ExploreResult<MREFLPMove, MREFLPSolution, MREFLPInstance> explore(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.explore(s, false, true);
    }
    @Override public Optional<MREFLPMove> getRandomMove(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.randomSwap(s);
    }
}
