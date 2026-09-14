package es.urjc.etsii.grafo.tsptw.constructives;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepair;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWUnrestrictedInsertShake;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Objects;

/** Feasibility VNS. If the deadline expires, the returned complete tour may still be infeasible. */
public class TSPTWFeasibleConstructive extends Constructive<TSPTWSolution, TSPTWInstance> {
    private final TSPTWRandomConstructive initial;
    private final TSPTWFeasibilityRepair repair;
    private final TSPTWUnrestrictedInsertShake perturbation;

    @AutoconfigConstructor
    public TSPTWFeasibleConstructive(TSPTWRandomConstructive initial, TSPTWFeasibilityRepair repair,
                                   TSPTWUnrestrictedInsertShake perturbation) {
        this.initial = Objects.requireNonNull(initial);
        this.repair = Objects.requireNonNull(repair);
        this.perturbation = Objects.requireNonNull(perturbation);
    }

    public TSPTWFeasibleConstructive() {
        this(new TSPTWRandomConstructive(), new TSPTWFeasibilityRepair(), new TSPTWUnrestrictedInsertShake());
    }

    @Override
    public TSPTWSolution construct(TSPTWSolution empty) {
        var instance = empty.getInstance();
        TSPTWUtil.requireMinimumSize(instance);
        int levelMax = instance.n() / 2;
        TSPTWSolution solution;
        do {
            int level = 1;
            solution = initial.construct(empty);
            solution.assert_solution();
            repair.repair(solution);
            var candidate = solution.cloneSolution();
            while (solution.constraint_violations() > 0 && level < levelMax && !TimeControl.isTimeUp()) {
                candidate = perturbation.shake(candidate, level);
                repair.repair(candidate);
                if (candidate.infeasibility() < solution.infeasibility()) {
                    solution = candidate.cloneSolution();
                    level = 1;
                } else {
                    candidate = solution.cloneSolution();
                    level++;
                }
            }
            empty = new TSPTWSolution(instance);
        } while (solution.constraint_violations() > 0 && !TimeControl.isTimeUp());
        return solution;
    }

    @Override
    public String toString() {
        return "TSPTWFeasibleConstructive{initial=" + initial + ", repair=" + repair
                + ", perturbation=" + perturbation + "}";
    }
}
