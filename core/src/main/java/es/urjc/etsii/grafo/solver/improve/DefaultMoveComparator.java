package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Optional;


/**
 * Default move comparator, used if the user does not provide a custom implementation for the problem.
 *
 * @param <M> Move class
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class DefaultMoveComparator<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> extends MoveComparator<M, S, I> {
    private boolean maximizing;

    /**
     * Create a new default move comparator that orders movements taking into account if this is a maximization or minimization problem
     *
     * @param maximizing True if maximizing, false if minimizing
     */
    public DefaultMoveComparator(boolean maximizing) {
        this.maximizing = maximizing;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<M> getStrictBestMove(M m1, M m2) {
        double m1Value = m1.getValue();
        double m2Value = m2.getValue();
        if(DoubleComparator.equals(m1Value, m2Value)){
            return Optional.empty();
        }
        if(maximizing){
            if (DoubleComparator.isGreaterThan(m1Value, m2Value)) {
                return Optional.of(m1);
            } else {
                return Optional.of(m2);
            }
        } else {
            if (DoubleComparator.isGreaterThan(m1Value, m2Value)) {
                return Optional.of(m2);
            } else {
                return Optional.of(m1);
            }
        }
    }
}
