package es.urjc.etsii.grafo.solver.services.events.types;

import java.util.List;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class ExperimentStartedEvent extends MorkEvent{
    private final String experimentName;
    private final List<String> instanceNames;

    /**
     * Create a new ExperimentStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceNames instance names
     */
    public ExperimentStartedEvent(String experimentName, List<String> instanceNames) {
        this.experimentName = experimentName;
        this.instanceNames = instanceNames;
    }

    /**
     * Get experiment name
     *
     * @return experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Get instance names
     *
     * @return list of instance names
     */
    public List<String> getInstanceNames() {
        return instanceNames;
    }
}
