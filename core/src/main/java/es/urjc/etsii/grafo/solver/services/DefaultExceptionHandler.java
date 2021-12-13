package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Default exception handler.
 * Executes when the user does not provide an exception handler implementation,
 * and an unhandled exception reaches the Mork executor.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class DefaultExceptionHandler<S extends Solution<S,I>, I extends Instance> extends ExceptionHandler<S,I>{
    private static final Logger logger = Logger.getLogger(DefaultExceptionHandler.class.getName());

    /**
     * {@inheritDoc}
     *
     * Handle exception that is not controlled in the user code and reaches our executor.
     * Behaviour can be customized or changed by extending the ExceptionHandler class.
     */
    public void handleException(String experimentName, Exception e, Optional<S> sOptional, I i, Algorithm<S,I> algorithm, IOManager<S, I> io){
        logger.severe(String.format("Error while solving instance %s with algorithm %s, skipping. Exception message: %s", i.getName(), algorithm.toString(), e.getMessage()));
        String stackTrace = getStackTrace(e);
        logger.severe("Stacktrace: " + stackTrace);
        sOptional.ifPresent(s -> logger.severe("Last executed movements: " + s.lastExecutesMoves()));
        io.exportError(experimentName, algorithm, i, e, stackTrace);
    }

    /**
     * Generate stacktrace as string from throwable
     *
     * @param t Throwable
     * @return Stacktrace as string
     */
    public String getStackTrace(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
