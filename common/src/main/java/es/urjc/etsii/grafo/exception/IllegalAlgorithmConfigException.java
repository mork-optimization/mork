package es.urjc.etsii.grafo.exception;

import java.io.Serial;

/**
 * Thrown by any algorithm component when the combination of parameters given is not valid
 */
public class IllegalAlgorithmConfigException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Initialize the exception
     * @param explanation Explanation
     */
    public IllegalAlgorithmConfigException(String explanation) {
        super(explanation);
    }
}
