package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.executors.WorkUnitResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight event triggered each time an algorithm finishes creating a result.
 */
public record SolutionGeneratedEvent(
        UUID resultId,
        boolean success,
        String experimentName,
        String instanceName,
        String algorithmName,
        String iteration,
        Map<String, Double> objectives,
        double score,
        long executionTime,
        long timeToBest
) implements MorkEvent {

    public SolutionGeneratedEvent {
        objectives = Collections.unmodifiableMap(new LinkedHashMap<>(objectives));
    }

    /**
     * Create event from stored result data.
     *
     * @param result stored work-unit result
     */
    public SolutionGeneratedEvent(WorkUnitResult<?, ?> result) {
        this(
                result.resultId(),
                result.success(),
                result.experimentName(),
                result.instanceId(),
                result.algorithm().getName(),
                result.iteration(),
                result.objectives(),
                result.mainObjectiveValue(),
                result.executionTime(),
                result.timeToTarget()
        );
    }
}
