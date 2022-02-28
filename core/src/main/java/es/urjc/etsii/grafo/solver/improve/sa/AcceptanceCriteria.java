package es.urjc.etsii.grafo.solver.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Simulated annealing acceptance criteria. A move can be applied if it either improves the objective function score
 * or if the acceptance function returns true.
 * @param <M> Move class
 * @param <S> Solution class
 * @param <I> Instance class
 */
@FunctionalInterface
public interface AcceptanceCriteria<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {

    /**
     * Simulated annealing acceptance criteria. A move can be applied if it either improves the objective function score
     * or if the acceptance function returns true.
     * @param move move to test if accepted
     * @param currentTemperature current temperature
     * @return true to accept, false to reject
     */
    boolean accept(M move, double currentTemperature);
}
