package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Time limit
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class TimeLimitCalculator<S extends Solution<S,I>, I extends Instance> {

    /**
     * Calculate timelimit in milliseconds, can be customized per instance and algorithm
     * @param instance current instance being solved
     * @param algorithm algorithm that is going to be executed
     */
    public abstract long timeLimitInMillis(I instance, Algorithm<S,I> algorithm);
}
