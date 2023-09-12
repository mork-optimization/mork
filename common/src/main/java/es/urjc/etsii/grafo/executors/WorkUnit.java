package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public record WorkUnit<S extends Solution<S,I>, I extends Instance>(String experimentName, String instancePath, Algorithm<S, I> algorithm, int i) {
}
