package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.HashMap;
import java.util.Map;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(WorkUnit<S,I> workUnit, S solution, long executionTime, long timeToTarget, Map<String, Object> userDefinedProperties) {
    public WorkUnitResult(WorkUnit<S, I> workUnit, S solution, long executionTime, long timeToTarget) {
        this(workUnit, solution, executionTime, timeToTarget, calculateProperties(solution));
    }

    public static <S extends Solution<S, I>, I extends Instance> Map<String, Object> calculateProperties(S solution) {
        var calculatedProperties = new HashMap<String, Object>();
        for(var entry: solution.customProperties().entrySet()){
            calculatedProperties.put(entry.getKey(), entry.getValue().apply(solution));
        }
        return calculatedProperties;
    }
}
