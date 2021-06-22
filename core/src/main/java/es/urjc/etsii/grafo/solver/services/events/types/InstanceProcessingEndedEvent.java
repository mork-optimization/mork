package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered after ending an experiment
 */
public class InstanceProcessingEndedEvent extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final long executionTime;

    public InstanceProcessingEndedEvent(String experimentName, String instanceName, long executionTime) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.executionTime = executionTime;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getInstanceName() {
        return instanceName;
    }
}
