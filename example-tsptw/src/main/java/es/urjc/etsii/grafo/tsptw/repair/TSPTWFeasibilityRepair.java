package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;

@AlgorithmComponent
public class TSPTWFeasibilityRepair {
    private final List<TSPTWRepairPhase> phases;

    @AutoconfigConstructor
    public TSPTWFeasibilityRepair(@ComponentParam(min = 4, max = 4) List<TSPTWRepairPhase> phases) {
        this.phases = TSPTWUtil.distinctComponents(phases, 4, 4);
    }

    public TSPTWFeasibilityRepair() {
        this(List.of(new TSPTWBackwardViolated(), new TSPTWForwardNonviolated(),
                new TSPTWForwardViolated(), new TSPTWBackwardNonviolated()));
    }

    @TimeStats
    public void repair(TSPTWSolution solution) {
        double before;
        do {
            before = solution.infeasibility();
            for (var phase : phases) {
                if (solution.constraint_violations() == 0 || TimeControl.isTimeUp()) return;
                phase.repair(solution);
            }
        } while (solution.infeasibility() < before && !TimeControl.isTimeUp());
    }

    @Override
    public String toString() {
        return "TSPTWFeasibilityRepair{phases=" + phases + "}";
    }
}
