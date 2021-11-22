package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered after ending an experiment
 */
public class InstanceProcessingEndedEvent extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final long executionTime;

    /**
     * Create a new instance processing ended event.
     * The event is triggered by the framework when an instance has been solved by all the algorithms.
     * @param experimentName Current experiment name
     * @param instanceName Instance name
     * @param executionTime Accumulated execution time in nanoseconds for this instance
     */
    public InstanceProcessingEndedEvent(String experimentName, String instanceName, long executionTime) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.executionTime = executionTime;
    }

    /**
     * Get current experiment name
     * @return experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Get execution time in nanos
     * @return execution time in nanos
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Get instance name
     * @return instance name
     */
    public String getInstanceName() {
        return instanceName;
    }
}
