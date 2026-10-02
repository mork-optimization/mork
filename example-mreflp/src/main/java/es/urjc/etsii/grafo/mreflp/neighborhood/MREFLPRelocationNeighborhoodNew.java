package es.urjc.etsii.grafo.mreflp.neighborhood;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.neighborhood.*;
import java.util.Optional;

public final class MREFLPRelocationNeighborhoodNew extends RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance> {
    @AutoconfigConstructor public MREFLPRelocationNeighborhoodNew() {}
    @Override public ExploreResult<MREFLPMove, MREFLPSolution, MREFLPInstance> explore(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.explore(s, true, false);
    }
    @Override public Optional<MREFLPMove> getRandomMove(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.randomRelocation(s);
    }
}
