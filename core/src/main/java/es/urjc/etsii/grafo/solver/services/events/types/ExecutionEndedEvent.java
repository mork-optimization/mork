package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered when solver execution ends
 */
public class ExecutionEndedEvent extends MorkEvent {
    private final long executionTime;

    public ExecutionEndedEvent(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
