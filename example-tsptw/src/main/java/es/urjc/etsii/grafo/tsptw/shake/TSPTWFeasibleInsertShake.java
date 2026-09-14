package es.urjc.etsii.grafo.tsptw.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** Preserves feasibility, including when the deadline interrupts a perturbation. */
public class TSPTWFeasibleInsertShake extends Shake<TSPTWSolution, TSPTWInstance> {
    @AutoconfigConstructor
    public TSPTWFeasibleInsertShake() {}

    @Override
    public TSPTWSolution shake(TSPTWSolution solution, int k) {
        solution.perturb_1shift_feasible(k);
        return solution;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
