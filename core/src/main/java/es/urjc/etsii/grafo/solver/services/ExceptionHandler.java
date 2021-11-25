package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.util.Optional;

/**
 * Abstract class to provide behaviour when an uncontrolled exception reaches executor code.
 *
 * @see DefaultExceptionHandler for an example implementation
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class ExceptionHandler<S extends Solution<S,I>, I extends Instance> {
    /**
     * What should be done when there is an unhandled exception in the user algorithm implementation?
     *
     * @param experimentName Experiment name
     * @param e Thrown exception
     * @param s Current solution, if available
     * @param i Current instance
     * @param algorithm Current algorithm
     * @param io IOManager, to optionally persist for example exception data.
     */
    public abstract void handleException(String experimentName, Exception e, Optional<S> s, I i, Algorithm<S,I> algorithm, IOManager<S, I> io);
}
