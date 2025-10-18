package es.urjc.etsii.grafo.algorithms.vns;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.function.BiFunction;

/**
 * Calculates K value for each VNS step.
 * See {@link DefaultVNSNeighChange} for an example implementation.
 */
@FunctionalInterface
public interface VNSNeighChange<S extends Solution<S, I>, I extends Instance> extends BiFunction<S, Integer, Integer> {
    int STOPNOW = -1;

    /**
     * Implements the neighborhood change strategy for VNS.
     * K starts at 0 and can be incremented or modified based on the solution's state.
     *
     * @param solution  Current solution, provided as a parameter so K can be adapted or scaled to instance size.
     * @param originalK Current k strength. Starts at 0 and increments by 1 each time the solution does not improve.
     * @return new K value. Return {@link VNSNeighChange#STOPNOW} to stop when the VNS should terminate
     */
    Integer apply(S solution, Integer originalK);
}
