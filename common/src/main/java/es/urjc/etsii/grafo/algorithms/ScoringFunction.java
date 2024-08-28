package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

@FunctionalInterface
public interface ScoringFunction<S extends Solution<S,I>, I extends Instance> {
    double score(S solution);
}
