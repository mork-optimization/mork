package es.urjc.etsii.grafo.events.types;

import java.util.List;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class ExperimentStartedEvent extends MorkEvent{
    private final String experimentName;
    private final List<String> instancePaths;

    /**
     * Create a new ExperimentStartedEvent
     *
     * @param experimentName experiment name
     * @param instancePaths instance load paths
     */
    public ExperimentStartedEvent(String experimentName, List<String> instancePaths) {
        this.experimentName = experimentName;
        this.instancePaths = instancePaths;
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
     * Get instance load paths.
     *
     * @return list of instance load paths
     */
    public List<String> getInstanceNames() {
        return instancePaths;
    }
}
