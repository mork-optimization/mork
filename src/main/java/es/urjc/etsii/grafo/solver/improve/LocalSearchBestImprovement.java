package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Optional;
import java.util.stream.Stream;

public class LocalSearchBestImprovement<S extends Solution<I>,I extends Instance> extends LocalSearch<S,I> {

    public LocalSearchBestImprovement(MoveComparator<Move<S,I>> comparator, String lsType, Neighborhood<S,I>... ps){
        super(comparator, lsType, ps);
    }

    @Override
    protected Move<S,I> getMove(S s){
        Move<S,I> move = null;
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

    private Optional<Move<S,I>> getBest(Stream<Move<S,I>> stream){
        return stream.filter(Move::isValid).reduce((a, b) -> comparator.getBest(b,a));
    }


    @Override
    public String toString() {
        return "LSBest{type=" + lsType + "}";
    }
}
