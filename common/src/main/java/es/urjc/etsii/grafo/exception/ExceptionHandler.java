package es.urjc.etsii.grafo.exception;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;

/**
 * Abstract class to provide behaviour when an uncontrolled exception reaches executor code.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class ExceptionHandler<S extends Solution<S,I>, I extends Instance> {
    /**
     * What should be done when there is an unhandled exception in the user algorithm implementation?
     *
     * @param experimentName Experiment name
     * @param iteration Iteration if known, -1 if not
     * @param e Thrown exception
     * @param s Current solution, if available
     * @param i Current instance
     * @param algorithm Current algorithm
     */
    public abstract void handleException(String experimentName, int iteration, Exception e, Optional<S> s, I i, Algorithm<S,I> algorithm);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
