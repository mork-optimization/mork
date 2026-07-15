package es.urjc.etsii.grafo.events.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Triggers when an unhandled exception or throwable reaches the default Mork exception handler.
 */
public record ErrorEvent(
        @JsonIgnore Throwable throwable,
        String exceptionType,
        String message
) implements MorkEvent {

    /**
     * Create a new error event from a throwable
     *
     * @param t Throwable
     */
    public ErrorEvent(Throwable t) {
        this(t, t.getClass().getSimpleName(), t.getMessage());
    }
}
