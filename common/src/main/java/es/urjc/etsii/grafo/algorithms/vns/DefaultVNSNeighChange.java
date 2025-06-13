package es.urjc.etsii.grafo.algorithms.vns;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Default VNSNeighChange implementation that increments K one by one until it reaches maxK.
 * VNS stops when K reaches maxK.
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class DefaultVNSNeighChange<S extends Solution<S,I>, I extends Instance> implements VNSNeighChange<S,I>{

    private final int maxK;
    private final int increment;

    public DefaultVNSNeighChange(int maxK, int increment) {
        this.maxK = maxK;
        this.increment = increment;
    }

    @Override
    public Integer apply(S solution, Integer originalK) {
        if(originalK >= maxK) {
            return VNSNeighChange.STOPNOW;
        } else {
            return originalK + increment; // Neighborhood change = increment K, defaults to 1
        }
    }
}
