package es.urjc.etsii.grafo.tsptw.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** May violate time windows; used only by feasibility construction. */
public class TSPTWUnrestrictedInsertShake extends Shake<TSPTWSolution, TSPTWInstance> {
    @AutoconfigConstructor
    public TSPTWUnrestrictedInsertShake() {}

    @Override
    public TSPTWSolution shake(TSPTWSolution solution, int k) {
        solution.perturb_1shift(k);
        return solution;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
