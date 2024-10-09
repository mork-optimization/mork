package es.urjc.etsii.grafo.exception;

import java.io.Serial;

/**
 * Thrown when an invalid random usage is detected, which would affect experiment reproducibility, and suggests alternatives to use.
 */
public class InvalidRandomException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRandomException(String message) {
        super(message);
    }
}
