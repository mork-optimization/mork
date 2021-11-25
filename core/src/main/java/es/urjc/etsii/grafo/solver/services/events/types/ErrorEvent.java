package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggers when an unhandled exception or throwable reaches the default Mork exception handler.
 */
public class ErrorEvent extends MorkEvent {
    private final Throwable t;

    /**
     * Create a new error event from a throwable
     *
     * @param t Throwable
     */
    public ErrorEvent(Throwable t) {
        this.t = t;
    }

    /**
     * Get error event cause
     *
     * @return error cause
     */
    public Throwable getThrowable() {
        return t;
    }
}
