package es.urjc.etsii.grafo.events.types;

/**
 * Triggered when solver execution ends
 */
public record ExecutionEndedEvent(long executionTime, Outcome outcome) implements MorkEvent {
    public enum Outcome { SUCCEEDED, COMPLETED_WITH_ERRORS, FAILED, CANCELLED }
}
