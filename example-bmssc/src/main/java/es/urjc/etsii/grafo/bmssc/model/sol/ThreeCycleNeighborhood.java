package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Optional;
import java.util.function.Supplier;

public class ThreeCycleNeighborhood extends RandomizableNeighborhood<ThreeCycleMove, BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public ThreeCycleNeighborhood() {}

    @Override
    public ExploreResult<ThreeCycleMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution) {
        if (TimeControl.isTimeUp() || BMSSCNewUtil.eligibleClusters(solution, 1).length < 3) return ExploreResult.empty();
        int n = solution.getInstance().n;
        return BMSSCNewUtil.moves(new Supplier<ThreeCycleMove>() {
            int p = 0, q = 1, r = 2;
            boolean reverse;

            @Override
            public ThreeCycleMove get() {
                while (p < n - 2 && !TimeControl.isTimeUp()) {
                    if (q >= n - 1) {
                        q = ++p + 1;
                        r = q + 1;
                        continue;
                    }
                    if (r >= n) {
                        r = ++q + 1;
                        continue;
                    }
                    int third = r;
                    boolean direction = reverse;
                    if (reverse) r++;
                    reverse = !reverse;
                    if (solution.canSwap(p, q) && solution.canSwap(p, third) && solution.canSwap(q, third)) {
                        return new ThreeCycleMove(solution, p, q, third, direction);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<ThreeCycleMove> getRandomMove(BMSSCSolution solution) {
        if (TimeControl.isTimeUp()) return Optional.empty();
        int[] eligible = BMSSCNewUtil.eligibleClusters(solution, 1);
        if (eligible.length < 3) return Optional.empty();
        int[] clusters = BMSSCNewUtil.sample(eligible, 3);
        int p = CollectionUtil.pickRandom(solution.getCluster(clusters[0]));
        int q = CollectionUtil.pickRandom(solution.getCluster(clusters[1]));
        int r = CollectionUtil.pickRandom(solution.getCluster(clusters[2]));
        return Optional.of(new ThreeCycleMove(solution, p, q, r, RandomManager.getRandom().nextBoolean()));
    }
}
