package es.urjc.etsii.grafo.exception;

/**
 * Thrown when the maximum allocated resources are consumed,
 * or when even if they are not consumed,
 * it is estimated with high confidence that we cannot complete the operation.
 */
public class ResourceLimitException extends RuntimeException {

    /**
     * Initialize the exception
     * @param message Explanation
     */
    public ResourceLimitException(String message) {
        super(message);
    }
}
