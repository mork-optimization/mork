package es.urjc.etsii.grafo.events.types;

/**
 * Ping everyone, does nothing. Useful for testing, manually triggered by /ping
 */
public record PingEvent() implements MorkEvent {
}
