package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;

/**
 * Tries the selected repair subset, then the original four-phase repair if still infeasible.
 * Both stages respect the shared deadline; feasibility is not guaranteed within a finite budget.
 */
public class TSPTWFeasibilityRepairFallbackNew extends TSPTWFeasibilityRepairNew {
    private final TSPTWFeasibilityRepair fallback = new TSPTWFeasibilityRepair();

    @AutoconfigConstructor
    public TSPTWFeasibilityRepairFallbackNew(@ComponentParam(min = 1, max = 4) List<TSPTWRepairPhase> phases,
                                           @IntegerParam(min = 0, max = 8) int maxPasses) {
        super(phases, maxPasses);
    }

    public TSPTWFeasibilityRepairFallbackNew() {
        super();
    }

    @Override
    @TimeStats
    public void repair(TSPTWSolution solution) {
        super.repair(solution);
        if (solution.constraint_violations() > 0 && !TimeControl.isTimeUp()) {
            fallback.repair(solution);
        }
    }

    @Override
    public String toString() {
        return "TSPTWFeasibilityRepairFallbackNew{primary=" + super.toString() + ", fallback=" + fallback + "}";
    }
}
