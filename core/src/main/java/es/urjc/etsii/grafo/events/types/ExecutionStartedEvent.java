package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.FMode;

import java.util.List;

/**
 * Triggered when the solver starts.
 */
public class ExecutionStartedEvent extends MorkEvent {

    private final boolean maximizing;
    private final List<String> experimentNames;

    /**
     * Create a new ExecutionStartedEvent, triggered by the framework when the solver is ready to start.
     *
     * @param maximizing
     * @param experimentNames experiment names
     */
    public ExecutionStartedEvent(boolean maximizing, List<String> experimentNames) {
        this.maximizing = maximizing;
        this.experimentNames = experimentNames;
    }

    /**
     * Create a new ExecutionStartedEvent, triggered by the framework when the solver is ready to start.
     *
     * @param fMode           mode
     * @param experimentNames experiment names
     */
    public ExecutionStartedEvent(FMode fMode, List<String> experimentNames) {
        this(fMode == FMode.MAXIMIZE, experimentNames);
    }

    /**
     * List of all experiments to execute
     *
     * @return names of the experiments to execute as a list
     */
    public List<String> getExperimentNames() {
        return experimentNames;
    }

    /**
     * Is this a maximization or a minimization problem?
     *
     * @return true if maximizing, false if minimizing
     */
    public boolean isMaximizing() {
        return maximizing;
    }
}
