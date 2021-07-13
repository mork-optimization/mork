package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;
import java.util.stream.Stream;

public class LocalSearchBestImprovement<M extends Move<S,I>, S extends Solution<I>,I extends Instance> extends LocalSearch<M,S,I> {

    public LocalSearchBestImprovement(MoveComparator<M,S,I> comparator, String lsType, Neighborhood<M,S,I>... ps){
        super(comparator, lsType, ps);
    }

    public LocalSearchBestImprovement(boolean maximizing, String lsType, Neighborhood<M,S,I>... ps){
        super(maximizing, lsType, ps);
    }

    @Override
    protected M getMove(S s){
        M move = null;
        for (var provider : providers) {
            var _move = getBest(provider.stream(s));
            if(_move.isEmpty()) continue;
            if (move == null) {
                move = _move.get();
            } else {
                move = this.comparator.getBest(move, _move.get());
            }
        }
        return move;
    }

    private Optional<M> getBest(Stream<M> stream){
        return stream.filter(Move::isValid).reduce((a, b) -> comparator.getBest(b,a));
    }


    @Override
    public String toString() {
        return "LSBest{t=" + lsType + "}";
    }
}
