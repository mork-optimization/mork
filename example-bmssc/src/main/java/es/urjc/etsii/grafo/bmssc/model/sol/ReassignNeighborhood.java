package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

// Internal to strategic oscillation: these moves may violate the original balance constraint.
public class ReassignNeighborhood extends Neighborhood<ReassignMove, BMSSCSolution, BMSSCInstance> {
    @Override
    public ExploreResult<ReassignMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution) {
        return explore(solution, -1);
    }

    public ExploreResult<ReassignMove, BMSSCSolution, BMSSCInstance> exploreRepair(BMSSCSolution solution, int source) {
        if (solution.getClusterSize(source) <= solution.getInstance().getClusterSize(source)) return ExploreResult.empty();
        return explore(solution, source);
    }

    private ExploreResult<ReassignMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution, int repairSource) {
        var instance = solution.getInstance();
        var moves = new Spliterators.AbstractSpliterator<ReassignMove>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            int point = 0;
            int cluster = 0;

            @Override
            public boolean tryAdvance(Consumer<? super ReassignMove> action) {
                while (point < instance.n && !TimeControl.isTimeUp()) {
                    if (cluster == instance.k) {
                        point++;
                        cluster = 0;
                        continue;
                    }
                    int target = cluster++;
                    if (!solution.canReassign(point, target)) continue;
                    if (repairSource >= 0 && (solution.clusterOf(point) != repairSource
                            || solution.getClusterSize(target) >= instance.getClusterSize(target))) continue;
                    action.accept(new ReassignMove(solution, point, target));
                    return true;
                }
                return false;
            }
        };
        return ExploreResult.fromStream(StreamSupport.stream(moves, false));
    }
}
