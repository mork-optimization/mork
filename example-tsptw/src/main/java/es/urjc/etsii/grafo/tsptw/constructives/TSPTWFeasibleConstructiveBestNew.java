package es.urjc.etsii.grafo.tsptw.constructives;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepair;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairFullNew;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWUnrestrictedInsertShake;
import es.urjc.etsii.grafo.util.TimeControl;

/** Original feasibility VNS with an independent best repaired tour retained across restarts. */
public class TSPTWFeasibleConstructiveBestNew extends TSPTWFeasibleConstructive {
    private final TSPTWRandomConstructive initial;
    private final TSPTWFeasibilityRepair repair;
    private final TSPTWUnrestrictedInsertShake perturbation;

    @AutoconfigConstructor
    public TSPTWFeasibleConstructiveBestNew(TSPTWRandomConstructive initial, TSPTWFeasibilityRepair repair,
                                          TSPTWUnrestrictedInsertShake perturbation) {
        super(initial, repair, perturbation);
        this.initial = initial;
        this.repair = repair;
        this.perturbation = perturbation;
    }

    public TSPTWFeasibleConstructiveBestNew() {
        this(new TSPTWRandomConstructive(), new TSPTWFeasibilityRepairFullNew(), new TSPTWUnrestrictedInsertShake());
    }

    @Override
    public TSPTWSolution construct(TSPTWSolution empty) {
        var instance = empty.getInstance();
        TSPTWUtil.requireMinimumSize(instance);
        int levelMax = instance.n() / 2;
        TSPTWSolution best = null;
        TSPTWSolution solution;
        do {
            int level = 1;
            solution = initial.construct(empty);
            solution.assert_solution();
            repair.repair(solution);
            if (best == null || TSPTWUtil.compareForRepair(solution, best) < 0) {
                best = solution.cloneSolution();
            }
            var candidate = solution.cloneSolution();
            while (solution.constraint_violations() > 0 && level < levelMax && !TimeControl.isTimeUp()) {
                candidate = perturbation.shake(candidate, level);
                repair.repair(candidate);
                // Retain tie-break improvements even when the original acceptance rule rejects them.
                if (TSPTWUtil.compareForRepair(candidate, best) < 0) {
                    best = candidate.cloneSolution();
                }
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
        return best;
    }

    @Override
    public String toString() {
        return "TSPTWFeasibleConstructiveBestNew{initial=" + initial + ", repair=" + repair
                + ", perturbation=" + perturbation + "}";
    }
}
