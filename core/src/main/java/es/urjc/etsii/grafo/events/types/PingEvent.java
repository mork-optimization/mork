package es.urjc.etsii.grafo.events.types;

/**
 * Ping everyone, does nothing. Useful for testing, manually triggered by /ping
 */
public class PingEvent extends MorkEvent {
    String message = "Ping!";

    /**
     * Create a PingEvent
     */
    public PingEvent() {
        super();
    }
}
