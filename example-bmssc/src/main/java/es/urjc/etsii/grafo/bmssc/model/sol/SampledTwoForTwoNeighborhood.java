package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Supplier;

/** A bounded sample, not an exhaustive two-for-two neighborhood. Duplicate draws count as attempts. */
public class SampledTwoForTwoNeighborhood extends RandomizableNeighborhood<TwoForTwoMove, BMSSCSolution, BMSSCInstance> {
    private final int samples;

    @AutoconfigConstructor
    public SampledTwoForTwoNeighborhood(@IntegerParam(min = 16, max = 4096) int samples) {
        if (samples < 1) throw new IllegalArgumentException("Sampling attempts must be positive");
        this.samples = samples;
    }

    @Override
    public ExploreResult<TwoForTwoMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution) {
        return BMSSCNewUtil.moves(new Supplier<TwoForTwoMove>() {
            final HashSet<TwoForTwoMove> seen = new HashSet<>();
            int attempts;

            @Override
            public TwoForTwoMove get() {
                while (attempts++ < samples && !TimeControl.isTimeUp()) {
                    var optional = getRandomMove(solution);
                    if (optional.isEmpty()) return null;
                    var move = optional.get();
                    if (seen.add(move)) return move;
                }
                return null;
            }
        });
    }

    @Override
    public Optional<TwoForTwoMove> getRandomMove(BMSSCSolution solution) {
        if (TimeControl.isTimeUp()) return Optional.empty();
        int[] eligible = BMSSCNewUtil.eligibleClusters(solution, 2);
        if (eligible.length < 2) return Optional.empty();
        int[] clusters = BMSSCNewUtil.sample(eligible, 2);
        int[] left = BMSSCNewUtil.sample(CollectionUtil.toIntArray(solution.getCluster(clusters[0])), 2);
        int[] right = BMSSCNewUtil.sample(CollectionUtil.toIntArray(solution.getCluster(clusters[1])), 2);
        return Optional.of(new TwoForTwoMove(solution, left[0], left[1], right[0], right[1]));
    }

    @Override
    public String toString() { return "SampledTwoForTwoNeighborhood{samples=" + samples + "}"; }
}
