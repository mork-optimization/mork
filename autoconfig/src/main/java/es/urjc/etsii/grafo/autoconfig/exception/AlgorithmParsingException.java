package es.urjc.etsii.grafo.autoconfig.exception;

public class AlgorithmParsingException extends RuntimeException {

    public AlgorithmParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlgorithmParsingException(String message) {
        super(message);
    }
}
