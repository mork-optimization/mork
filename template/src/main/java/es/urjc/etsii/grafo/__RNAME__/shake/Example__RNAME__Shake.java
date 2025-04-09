package es.urjc.etsii.grafo.__RNAME__.shake;

import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.shake.Shake;

public class Example__RNAME__Shake extends Shake<__RNAME__Solution, __RNAME__Instance> {

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
    public __RNAME__Solution shake(__RNAME__Solution solution, int k) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
