package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Calculate the distance (for example, the minimum number of moves to apply to transform solution A in solution B)
 * between any two given solutions for the current problem. Solution distance MUST follow the reflective constraint:
 * distance(a, b) == distance(b, a)
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class SolutionDistance<S extends Solution<S, I>, I extends Instance> {

    /**
     * Calculate distances between given solutions
     * @param sa first solution
     * @param sb second solution
     * @return distance matrix with distances between any two solutions
     */
    public abstract double distances(S sa, S sb);
}
