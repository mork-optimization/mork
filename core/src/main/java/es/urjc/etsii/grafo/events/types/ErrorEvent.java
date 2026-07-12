package es.urjc.etsii.grafo.events.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Triggers when an unhandled exception or throwable reaches the default Mork exception handler.
 */
public class ErrorEvent extends MorkEvent {
    private final Throwable t;
    private final String exceptionType;
    private final String message;

    /**
     * Create a new error event from a throwable
     *
     * @param t Throwable
     */
    public ErrorEvent(Throwable t) {
        this.t = t;
        this.exceptionType = t.getClass().getSimpleName();
        this.message = t.getMessage();
    }

    /**
     * Get error event cause
     *
     * @return error cause
     */
    @JsonIgnore
    public Throwable getThrowable() {
        return t;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }
}
