package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered after ending an experiment
 */
public class ExperimentEndedEvent extends MorkEvent {
    private final String experimentName;
    private final long executionTime;
    private final long experimentStartTime;

    /**
     * Create a new experiment ended event
     *  @param experimentName current experiment name
     * @param executionTime execution time in nanos
     * @param experimentStartTime experiment start timestamp
     */
    public ExperimentEndedEvent(String experimentName, long executionTime, long experimentStartTime) {
        this.experimentName = experimentName;
        this.executionTime = executionTime;
        this.experimentStartTime = experimentStartTime;
    }

    /**
     * Experiment name
     *
     * @return experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Execution time in nanoseconds
     *
     * @return execution time in nanoseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Get experiment start time as a timestamp
     *
     * @return start time as a timestamp
     */
    public long getExperimentStartTime() {
        return experimentStartTime;
    }
}
