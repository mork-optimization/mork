package es.urjc.etsii.grafo.solver.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(WorkUnit<S,I> workUnit, S solution, long executionTime, long timeToTarget) {
}
