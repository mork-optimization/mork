package es.urjc.etsii.grafo.events.types;

/**
 * Triggered when solver execution ends
 */
public record ExecutionEndedEvent(long executionTime) implements MorkEvent {
}
