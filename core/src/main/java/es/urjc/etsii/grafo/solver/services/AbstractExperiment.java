package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.util.List;

@InheritedComponent
public abstract class AbstractExperiment<S extends Solution<I>, I extends Instance> {

    private final boolean maximizing;

    protected AbstractExperiment(boolean maximizing) {
        this.maximizing = maximizing;
    }

    public boolean isMaximizing() {
        return maximizing;
    }

    public abstract List<Algorithm<S, I>> getAlgorithms();

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
