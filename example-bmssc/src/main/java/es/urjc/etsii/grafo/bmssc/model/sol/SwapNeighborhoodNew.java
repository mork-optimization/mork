package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Optional;
import java.util.function.Supplier;

/** Copy of SwapNeighborhood with random sampling for Mork's RandomMoveShake. */
public class SwapNeighborhoodNew extends RandomizableNeighborhood<SwapMove, BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public SwapNeighborhoodNew() {}

    protected int[] pointOrder(BMSSCSolution solution) {
        int[] points = new int[solution.getInstance().n];
        for (int p = 0; p < points.length; p++) points[p] = p;
        return points;
    }

    @Override
    public ExploreResult<SwapMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution) {
        if (TimeControl.isTimeUp()) return ExploreResult.empty();
        int[] points = pointOrder(solution);
        return BMSSCNewUtil.moves(new Supplier<SwapMove>() {
            int p = 0, q = 1;

            @Override
            public SwapMove get() {
                while (p < points.length - 1 && !TimeControl.isTimeUp()) {
                    if (q == points.length) {
                        q = ++p + 1;
                        continue;
                    }
                    int first = points[p], second = points[q++];
                    if (solution.canSwap(first, second)) return new SwapMove(solution, first, second);
                }
                return null;
            }
        });
    }

    @Override
    public Optional<SwapMove> getRandomMove(BMSSCSolution solution) {
        if (TimeControl.isTimeUp()) return Optional.empty();
        int[] eligible = BMSSCNewUtil.eligibleClusters(solution, 1);
        if (eligible.length < 2) return Optional.empty();
        int[] clusters = BMSSCNewUtil.sample(eligible, 2);
        int first = CollectionUtil.pickRandom(solution.getCluster(clusters[0]));
        int second = CollectionUtil.pickRandom(solution.getCluster(clusters[1]));
        return Optional.of(new SwapMove(solution, first, second));
    }
}
