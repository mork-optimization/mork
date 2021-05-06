package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Optional;

public class DefaultMoveComparator<M extends Move<S,I>, S extends Solution<I>, I extends Instance> extends MoveComparator<M, S, I> {
    private boolean maximizing;

    public DefaultMoveComparator(boolean maximizing) {
        this.maximizing = maximizing;
    }

    @Override
    public Optional<M> getStrictBestMove(M m1, M m2) {
        if(DoubleComparator.equals(m1.getValue(), m2.getValue())){
            return Optional.empty();
        }
        if(maximizing){
            if (DoubleComparator.isGreaterThan(m1.getValue(), m2.getValue())) {
                return Optional.of(m1);
            } else {
                return Optional.of(m2);
            }
        } else {
            if (DoubleComparator.isGreaterThan(m1.getValue(), m2.getValue())) {
                return Optional.of(m2);
            } else {
                return Optional.of(m1);
            }
        }
    }
}
