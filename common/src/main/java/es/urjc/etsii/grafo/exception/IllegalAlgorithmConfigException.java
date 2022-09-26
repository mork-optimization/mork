package es.urjc.etsii.grafo.exception;

/**
 * Thrown by any algorithm component when the combination of parameters given is not valid
 */
public class IllegalAlgorithmConfigException extends IllegalArgumentException {

    /**
     * Initialize the exception
     * @param explanation Explanation
     */
    public IllegalAlgorithmConfigException(String explanation) {
        super(explanation);
    }
}
