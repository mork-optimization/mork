package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;

/**
 * Implement this class if experiments should be run until a time limit is reached, instead of a predefined number of iterations.
 */
public interface TimeProvider<I extends Instance> {

    /**
     * Get the time limit for the given instance
     * @param instance Instance
     * @return Time limit in nanoseconds
     */
    long getTimeInNanos(I instance);
}
