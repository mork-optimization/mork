package es.urjc.etsii.grafo.io.serializers;

/**
 * Raised when a result serializer fails while exporting experiment results.
 */
public class SerializerExecutionException extends RuntimeException {

    /**
     * Create a serializer execution exception.
     *
     * @param message detailed failure context
     * @param cause root failure
     */
    public SerializerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
