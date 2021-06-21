package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered after ending an experiment
 */
public class ExperimentEndedEvent extends MorkEvent {
    private final String experimentName;
    private final long executionTime;

    public ExperimentEndedEvent(String experimentName, long executionTime) {
        this.experimentName = experimentName;
        this.executionTime = executionTime;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
