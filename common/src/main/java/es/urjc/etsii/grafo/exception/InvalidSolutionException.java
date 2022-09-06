package es.urjc.etsii.grafo.exception;

/**
 * Thrown if the solution is not valid at different solving stages
 */
public class InvalidSolutionException extends RuntimeException {

    /**
     * Create a new InvalidSolutionException with the given reason
     * @param cause why the solution is not valid
     */
    public InvalidSolutionException(String cause){
        super(cause);
    }
}
