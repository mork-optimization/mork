package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * This class should be a wrapper to communicate with jMetal
 * mono-objective algorithm.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class JMetalAlgorithm<S extends Solution<I>, I extends Instance> extends Algorithm<S,I> {
    @Override
    public S algorithm(I instance) {

        S solution = this.newSolution(instance);

        // TODO: integrate with jMetal.

        return solution;
    }
}
