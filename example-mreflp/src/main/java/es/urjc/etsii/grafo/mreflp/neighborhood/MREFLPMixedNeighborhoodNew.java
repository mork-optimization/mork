package es.urjc.etsii.grafo.mreflp.neighborhood;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.neighborhood.*;
import java.util.Optional;

public final class MREFLPMixedNeighborhoodNew extends RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance> {
    private final double relocationProbability;
    @AutoconfigConstructor
    public MREFLPMixedNeighborhoodNew(@RealParam(min = 0, max = 1) double relocationProbability) {
        if (!(relocationProbability >= 0 && relocationProbability <= 1)) throw new IllegalArgumentException("Invalid relocation probability");
        this.relocationProbability = relocationProbability;
    }
    @Override public ExploreResult<MREFLPMove, MREFLPSolution, MREFLPInstance> explore(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.explore(s, true, true);
    }
    @Override public Optional<MREFLPMove> getRandomMove(MREFLPSolution s) {
        return MREFLPNeighborhoodUtil.randomMixed(s, relocationProbability);
    }
}
