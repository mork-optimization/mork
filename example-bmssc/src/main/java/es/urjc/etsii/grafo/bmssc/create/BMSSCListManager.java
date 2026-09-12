package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.List;

public class BMSSCListManager extends GRASPListManager<AssignMove, BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public BMSSCListManager() {}

    @Override
    public void beforeGRASP(BMSSCSolution solution) {
        var instance = solution.getInstance();
        if (solution.getNotAssignedPoints().size() != instance.n) {
            throw new IllegalArgumentException("Construction requires an empty solution");
        }
        if (TimeControl.isTimeUp()) return;
        int[] seeds = new int[instance.k];
        seeds[0] = RandomManager.getRandom().nextInt(instance.n);
        new AssignMove(solution, seeds[0], 0).execute(solution);
        // Keep farthest-first seeding, choosing the first point even when all distances tie at zero.
        for (int cluster = 1; cluster < instance.k; cluster++) {
            double maximum = Double.NEGATIVE_INFINITY;
            int chosen = -1;
            for (int point = 0; point < instance.n; point++) {
                if (solution.isAssigned(point)) continue;
                double minimum = Double.POSITIVE_INFINITY;
                for (int c = 0; c < cluster; c++) {
                    minimum = Math.min(minimum, instance.distance(seeds[c], point));
                }
                if (minimum > maximum) {
                    maximum = minimum;
                    chosen = point;
                }
            }
            seeds[cluster] = chosen;
            new AssignMove(solution, chosen, cluster).execute(solution);
        }
    }

    @Override
    public List<AssignMove> buildInitialCandidateList(BMSSCSolution solution) {
        var notAssigned = solution.getNotAssignedPoints();
        var instance = solution.getInstance();
        var candidates = new ArrayList<AssignMove>(notAssigned.size() * instance.k);
        for (int cluster = 0; cluster < instance.k; cluster++) {
            if (solution.isFullCluster(cluster)) continue;
            for (int point : notAssigned) {
                candidates.add(new AssignMove(solution, point, cluster));
            }
        }
        return candidates;
    }

    @Override
    public List<AssignMove> updateCandidateList(BMSSCSolution solution, AssignMove move,
                                              List<AssignMove> candidateList, int index) {
        // Every assignment changes denominators and the solution version: rebuild all move deltas.
        return buildInitialCandidateList(solution);
    }
}
