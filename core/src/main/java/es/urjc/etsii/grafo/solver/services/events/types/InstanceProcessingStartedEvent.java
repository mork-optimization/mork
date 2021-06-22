package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class InstanceProcessingStartedEvent extends MorkEvent{
    private final String experimentName;
    private final String instanceName;

    public InstanceProcessingStartedEvent(String experimentName, String instanceName) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getInstanceName() {
        return instanceName;
    }
}
