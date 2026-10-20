package es.urjc.etsii.grafo.autoconfig.autollm.execution;

/** A changed execution host invalidates the experiment, not the proposed components. */
public final class ExecutionTargetException extends RuntimeException {
    public ExecutionTargetException(String message) { super(message); }
}
