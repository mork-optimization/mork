package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggers when an unhandled exception or throwable reaches the default Mork exception handler.
 */
public class ErrorEvent extends MorkEvent {
    private final Throwable t;

    public ErrorEvent(Throwable t) {
        this.t = t;
    }

    public Throwable getThrowable() {
        return t;
    }
}
