package es.urjc.etsii.grafo.autoconfig.exception;

import java.io.Serial;

public class AlgorithmParsingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AlgorithmParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlgorithmParsingException(String message) {
        super(message);
    }
}
