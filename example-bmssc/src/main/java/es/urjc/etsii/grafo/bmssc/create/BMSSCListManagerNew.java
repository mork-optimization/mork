package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil.SeedStrategy;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Copy of BMSSCListManager with selectable seeding and cancellation-aware quota completion. */
public class BMSSCListManagerNew extends GRASPListManager<AssignMove, BMSSCSolution, BMSSCInstance> {
    private final SeedStrategy seedStrategy;

    @AutoconfigConstructor
    public BMSSCListManagerNew(@CategoricalParam(strings = {"FARTHEST_FIRST", "RANDOM"}) SeedStrategy seedStrategy) {
        this.seedStrategy = Objects.requireNonNull(seedStrategy);
    }

    @Override
    public void beforeGRASP(BMSSCSolution solution) {
        BMSSCNewUtil.seed(solution, seedStrategy);
    }

    @Override
    public List<AssignMove> buildInitialCandidateList(BMSSCSolution solution) {
        var candidates = new ArrayList<AssignMove>();
        for (int cluster = 0; cluster < solution.getInstance().k; cluster++) {
            if (solution.isFullCluster(cluster)) continue;
            for (int point : solution.getNotAssignedPoints()) {
                var move = new AssignMove(solution, point, cluster);
                // A singleton candidate avoids further greedy scans while guaranteeing completion.
                if (TimeControl.isTimeUp()) return List.of(move);
                candidates.add(move);
            }
        }
        return candidates;
    }

    @Override
    public List<AssignMove> updateCandidateList(BMSSCSolution solution, AssignMove move,
                                              List<AssignMove> candidateList, int index) {
        return buildInitialCandidateList(solution);
    }
}
