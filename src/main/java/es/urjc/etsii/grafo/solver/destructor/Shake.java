package es.urjc.etsii.grafo.solver.destructor;


import es.urjc.etsii.grafo.solution.Solution;

public interface Shake {
    void iteration(Solution s, int k);
}