package es.urjc.etsii.grafo.solver.services.events.types;

import java.util.List;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class ExperimentStartedEvent extends MorkEvent{
    private final String experimentName;
    private final List<String> instanceNames;

    public ExperimentStartedEvent(String experimentName, List<String> instanceNames) {
        this.experimentName = experimentName;
        this.instanceNames = instanceNames;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<String> getInstanceNames() {
        return instanceNames;
    }
}
