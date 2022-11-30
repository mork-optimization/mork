package es.urjc.etsii.grafo.exceptions;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.exception.InvalidRandomException;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.services.IOManager;
import es.urjc.etsii.grafo.solution.Solution;

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
    public void handleException(String experimentName, int iteration, Exception e, Optional<S> sOptional, I i, Algorithm<S,I> algorithm, IOManager<S, I> io){
        logger.severe(String.format("Error while solving instance %s with algorithm %s, iteration %s, skipping. Exception message: %s", i.getId(), algorithm.toString(), iteration, e.getMessage()));
        explain(e);
        String stackTrace = getStackTrace(e);
        logger.severe("Stacktrace: " + stackTrace);
        sOptional.ifPresent(s -> logger.severe("Last executed movements: " + s.lastExecutesMoves()));
        io.exportError(experimentName, algorithm, i, e, stackTrace);
    }

    private void explain(Throwable e) {
        if(e instanceof InvalidRandomException){
            logger.severe("The exception InvalidRandomException is generated when a method from the Java API that would break reproducibility is executed. See https://mork-optimization.readthedocs.io/en/latest/quickstart/bestpractices/#customized-random-generator for more information.");
        }
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
