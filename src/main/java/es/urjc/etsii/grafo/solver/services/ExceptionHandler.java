package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

@InheritedComponent
public abstract class ExceptionHandler<S extends Solution<I>, I extends Instance> {
    public abstract void handleException(Exception e, I i, Algorithm<S,I> algorithm);
}
