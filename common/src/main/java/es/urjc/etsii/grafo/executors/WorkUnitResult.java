package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(String experimentName, String instancePath, Algorithm<S,I> algorithm, String iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics) {
    public WorkUnitResult(String experimentName, String instancePath, Algorithm<S,I> algorithm, int iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics){
        this(experimentName, instancePath, algorithm, String.valueOf(iteration), solution, executionTime, timeToTarget, metrics);
    }

    public WorkUnitResult(WorkUnit<S, I> workUnit, S solution, long executionTime, long timeToTarget, MetricsStorage metrics) {
        this(workUnit.experimentName(), workUnit.instancePath(), workUnit.algorithm(), workUnit.i(), solution, executionTime, timeToTarget, metrics);
    }
}
