package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class DefaultExceptionHandler<S extends Solution<I>, I extends Instance> extends ExceptionHandler<S,I>{
    private static final Logger logger = Logger.getLogger(DefaultExceptionHandler.class.getName());

    public void handleException(Exception e, I i, Algorithm<S,I> algorithm, IOManager<S, I> io){
        logger.severe(String.format("Error while solving instance %s with algorithm %s, skipping. Exception message: %s", i.getName(), algorithm.toString(), e.getMessage()));
        logger.fine("Stacktrace: " + getStackTrace(e));
        io.exportError(algorithm, i, e);
    }

    private String getStackTrace(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
