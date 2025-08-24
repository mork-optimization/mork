package es.urjc.etsii.grafo.tsptw.shake;

import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.shake.Shake;

public class ExampleTSPTWShake extends Shake<TSPTWSolution, TSPTWInstance> {

    /**
     * Shake / perturbate a feasible solution.
     * Shake methods usually have two steps:
     * 1. Modify solution following a given strategy, may make it infeasible
     * 2. Repair the solution to ensure it is feasible before returning it
     * @param solution Solution to modify
     * @param k shake strength
     * @return feasible solution after shaking (and repairing, if required)
     */
    @Override
    public TSPTWSolution shake(TSPTWSolution solution, int k) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
