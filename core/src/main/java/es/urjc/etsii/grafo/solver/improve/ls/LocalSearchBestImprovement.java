package es.urjc.etsii.grafo.solver.improve.ls;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Local search procedures start from a given feasible solution and explore a determined neighborhood
 * in each iteration, replacing the current solution if a neighbor solution improves the objective
 * function of the current one. In this case, this local search procedure follows a Best Improvement strategy.
 *  The best improvement strategy is related to executing the best move of the given neighbors.
 *
 * @param <M> the type of move
 * @param <S> the type of problem solution
 * @param <I> the type of problem instances
 */
public class LocalSearchBestImprovement<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> extends LocalSearch<M, S, I> {

    /**
     * Build a new local search
     *
     * @param comparator comparator to determine between two solutions which one is better
     * @param ps neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearchBestImprovement(MoveComparator<M, S, I> comparator, Neighborhood<M, S, I>... ps) {
        super(comparator, ps);
    }

    /**
     * Build a new local search
     *
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearchBestImprovement(boolean maximizing, Neighborhood<M, S, I>... ps) {
        super(maximizing, ps);
    }

    /**
     * Build a new local search
     *
     * @param lsName     Local Search name. If present, toString works as name{}. If not, Classname{neigh=[neigborhoods],comp=comparator}
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    @SafeVarargs
    public LocalSearchBestImprovement(boolean maximizing, String lsName, Neighborhood<M, S, I>... ps) {
        super(maximizing, lsName, ps);
    }

    /**
     * {@inheritDoc}
     *
     * Get next move to execute.
     */
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

    /**
     * Get best move to execute.
     *
     * @param stream a sequence of possible moves
     * @return the best move of the stream
     */
    private Optional<M> getBest(Stream<M> stream) {
        return stream.filter(Move::isValid).reduce((a, b) -> comparator.getBest(b, a));
    }

}
