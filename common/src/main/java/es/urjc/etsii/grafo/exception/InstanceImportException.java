package es.urjc.etsii.grafo.exception;

/**
 * Exception thrown when there is any problem loading an instance
 */
public class InstanceImportException extends RuntimeException {
    public InstanceImportException(String message) {
        super(message);
    }

    public InstanceImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
