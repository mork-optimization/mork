package es.urjc.etsii.grafo.solution;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Optional;

/**
 * Move that can rebuild itself for the current solution state.
 * <p>
 * Refreshing must return a new move instance bound to the given solution, or an empty optional if the
 * old move is no longer valid for the current solution.
 *
 * @param <M> refreshed move type
 * @param <S> solution type
 * @param <I> instance type
 */
public interface RefreshableMove<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> {

    /**
     * Rebuild this move for the given solution state.
     *
     * @param solution current solution
     * @return refreshed move, or empty if the move is no longer valid
     */
    Optional<M> refresh(S solution);
}
