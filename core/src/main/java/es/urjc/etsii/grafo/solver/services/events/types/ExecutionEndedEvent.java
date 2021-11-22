package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered when solver execution ends
 */
public class ExecutionEndedEvent extends MorkEvent {
    private final long executionTime;

    /**
     * Create a new ExecutionEndedEvent providing the accumulated execution time.
     *
     * @param executionTime total execution time in nanoseconds
     */
    public ExecutionEndedEvent(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * Get accumulated execution time
     *
     * @return execution time in nanoseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
