package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.services.ExceptionHandler;

public record WorkUnit<S extends Solution<S,I>, I extends Instance>(String experimentName, String instanceName, Algorithm<S, I> algorithm, int i, ExceptionHandler<S,I> exceptionHandler) {
}
