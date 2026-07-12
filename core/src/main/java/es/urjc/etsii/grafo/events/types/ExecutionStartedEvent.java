package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.solution.Objective;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Triggered when the solver starts.
 */
public class ExecutionStartedEvent extends MorkEvent {

    private final Map<String, String> objectives;
    private final List<String> experimentNames;

    /**
     * Create a new ExecutionStartedEvent, triggered by the framework when the solver is ready to start.
     *
     * @param objectives           main objective being optimized
     * @param experimentNames experiment names
     */
    public ExecutionStartedEvent(Map<String, Objective<?, ?, ?>> objectives, List<String> experimentNames) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (var e : objectives.entrySet()) {
            map.putIfAbsent(e.getKey(), e.getValue().getFMode().name());
        }
        this.objectives = map;
        this.experimentNames = experimentNames;
    }

    /**
     * List of all experiments to execute
     *
     * @return names of the experiments to execute as a list
     */
    public List<String> getExperimentNames() {
        return experimentNames;
    }

    public Map<String, String> getObjectives() {
        return objectives;
    }
}
