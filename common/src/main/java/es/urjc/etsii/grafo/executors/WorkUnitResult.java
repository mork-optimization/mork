package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.metrics.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(String experimentName, Algorithm<S,I> algorithm, String iteration, S solution, long executionTime, long timeToTarget, Map<String, TreeSet<TimeValue>> metrics) {
    public WorkUnitResult(String experimentName, Algorithm<S,I> algorithm, int iteration, S solution, long executionTime, long timeToTarget, Map<String, TreeSet<TimeValue>> metrics){
        this(experimentName, algorithm, String.valueOf(iteration), solution, executionTime, timeToTarget, metrics);
    }

    public WorkUnitResult(WorkUnit<S, I> workUnit, S solution, long executionTime, long timeToTarget, Map<String, TreeSet<TimeValue>> metrics) {
        this(workUnit.experimentName(), workUnit.algorithm(), workUnit.i(), solution, executionTime, timeToTarget, metrics);
    }
}
