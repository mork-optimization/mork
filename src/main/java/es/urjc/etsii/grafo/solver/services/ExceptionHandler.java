package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

@InheritedComponent
public abstract class ExceptionHandler<S extends Solution<I>, I extends Instance> {
    public abstract void handleException(String experimentname, Exception e, I i, Algorithm<S,I> algorithm, IOManager<S, I> io);
}
