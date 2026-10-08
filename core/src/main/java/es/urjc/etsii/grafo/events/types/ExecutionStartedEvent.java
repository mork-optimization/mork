package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.solution.Objective;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Triggered when the solver starts.
 */
public record ExecutionStartedEvent(
        Map<String, String> objectives,
        List<String> experimentNames
) implements MorkEvent {

    public ExecutionStartedEvent {
        objectives = Collections.unmodifiableMap(new LinkedHashMap<>(objectives));
        experimentNames = List.copyOf(experimentNames);
    }

    /**
     * Create a new ExecutionStartedEvent, triggered by the framework when the solver is ready to start.
     *
     * @param objectives           main objective being optimized
     * @param experimentNames experiment names
     */
    public ExecutionStartedEvent(
            Map<String, Objective<?, ?, ?>> objectives,
            Collection<String> experimentNames
    ) {
        this(toObjectiveModes(objectives), List.copyOf(experimentNames));
    }

    private static Map<String, String> toObjectiveModes(Map<String, Objective<?, ?, ?>> objectives) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (var e : objectives.entrySet()) {
            map.putIfAbsent(e.getKey(), e.getValue().getFMode().name());
        }
        return map;
    }
}
