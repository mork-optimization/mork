package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;

/** Subset/pass-limited variant of TSPTWFeasibilityRepair; the original remains independently selectable. */
public class TSPTWFeasibilityRepairNew extends TSPTWFeasibilityRepair {
    private final List<TSPTWRepairPhase> phases;
    private final int maxPasses;

    @AutoconfigConstructor
    public TSPTWFeasibilityRepairNew(@ComponentParam(min = 1, max = 4) List<TSPTWRepairPhase> phases,
                                   @IntegerParam(min = 0, max = 8) int maxPasses) {
        this.phases = TSPTWUtil.distinctComponents(phases, 1, 4);
        if (maxPasses < 0 || maxPasses > 8) throw new IllegalArgumentException("Maximum passes must be in [0, 8]");
        this.maxPasses = maxPasses;
    }

    public TSPTWFeasibilityRepairNew() {
        this(List.of(new TSPTWBackwardViolated(), new TSPTWForwardNonviolated(),
                new TSPTWForwardViolated(), new TSPTWBackwardNonviolated()), 0);
    }

    @Override
    @TimeStats
    public void repair(TSPTWSolution solution) {
        int passes = 0;
        double before;
        do {
            before = solution.infeasibility();
            for (var phase : phases) {
                if (solution.constraint_violations() == 0 || TimeControl.isTimeUp()) return;
                phase.repair(solution);
            }
            passes++;
        } while (solution.infeasibility() < before && (maxPasses == 0 || passes < maxPasses) && !TimeControl.isTimeUp());
    }

    @Override
    public String toString() { return "TSPTWFeasibilityRepairNew{phases=" + phases + ", maxPasses=" + maxPasses + "}"; }
}
