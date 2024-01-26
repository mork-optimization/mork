package es.urjc.etsii.grafo.exception;

import java.io.Serial;

/**
 * Thrown if the solution is not valid at different solving stages
 */
public class InvalidSolutionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Create a new InvalidSolutionException with the given reason
     * @param cause why the solution is not valid
     */
    public InvalidSolutionException(String cause){
        super(cause);
    }
}
