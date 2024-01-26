package es.urjc.etsii.grafo.exception;

import java.io.Serial;

/**
 * Exception thrown when there is any problem loading an instance
 */
public class InstanceImportException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InstanceImportException(String message) {
        super(message);
    }

    public InstanceImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
