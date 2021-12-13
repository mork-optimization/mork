package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered after ending an experiment
 */
public class ExperimentEndedEvent extends MorkEvent {
    private final String experimentName;
    private final long executionTime;

    /**
     * Create a new experiment ended event
     *
     * @param experimentName current experiment name
     * @param executionTime execution time in nanos
     */
    public ExperimentEndedEvent(String experimentName, long executionTime) {
        this.experimentName = experimentName;
        this.executionTime = executionTime;
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
}
