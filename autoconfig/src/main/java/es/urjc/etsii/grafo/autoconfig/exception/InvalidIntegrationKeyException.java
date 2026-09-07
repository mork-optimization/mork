package es.urjc.etsii.grafo.autoconfig.exception;

/**
 * Raised when an internal IRACE request does not provide the expected integration key.
 */
public class InvalidIntegrationKeyException extends IllegalArgumentException {

    public InvalidIntegrationKeyException() {
        super("Invalid integration key");
    }
}
