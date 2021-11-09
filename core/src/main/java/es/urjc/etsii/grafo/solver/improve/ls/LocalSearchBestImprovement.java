package es.urjc.etsii.grafo.solver.improve.ls;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.Optional;
import java.util.stream.Stream;

public class LocalSearchBestImprovement<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends LocalSearch<M, S, I> {

    @SafeVarargs
    public LocalSearchBestImprovement(MoveComparator<M, S, I> comparator, Neighborhood<M, S, I>... ps) {
        super(comparator, ps);
    }

    @SafeVarargs
    public LocalSearchBestImprovement(boolean maximizing, Neighborhood<M, S, I>... ps) {
        super(maximizing, ps);
    }

    @SafeVarargs
    public LocalSearchBestImprovement(boolean maximizing, String lsName, Neighborhood<M, S, I>... ps) {
        super(maximizing, lsName, ps);
    }

    @Override
    public Optional<M> getMove(S s) {
        M move = null;
        for (var provider : this.providers) {
            var _move = getBest(provider.stream(s));
            if (_move.isEmpty()) continue;
            if (move == null) {
                move = _move.get();
            } else {
                move = this.comparator.getBest(move, _move.get());
            }
        }
        return move != null && move.improves() ? Optional.of(move) : Optional.empty();
    }

    private Optional<M> getBest(Stream<M> stream) {
        return stream.filter(Move::isValid).reduce((a, b) -> comparator.getBest(b, a));
    }

}
