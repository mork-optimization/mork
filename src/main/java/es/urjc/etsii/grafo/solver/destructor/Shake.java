package es.urjc.etsii.grafo.solver.destructor;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public interface Shake<S extends Solution<I>, I extends Instance> {
    void iteration(S s, int k);
}