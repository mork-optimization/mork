package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

@AlgorithmComponent
public abstract class SolutionCombinator<S extends Solution<S, I>, I extends Instance> {
    /**
     * Returns new reference set starting from current reference set
     * @param currentSet current reference set, DO NOT MODIFY
     * @return new reference set
     */
    public abstract List<S> newSet(S[] currentSet);
}
