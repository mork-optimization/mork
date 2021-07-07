package es.urjc.etsii.grafo.solver.services.events.types;

import java.util.List;

/**
 * Triggered when the solver starts
 */
public class ExecutionStartedEvent extends MorkEvent{
    private final List<String> experimentNames;

    public ExecutionStartedEvent(List<String> experimentNames) {
        this.experimentNames = experimentNames;
    }

    public List<String> getExperimentNames() {
        return experimentNames;
    }
}
