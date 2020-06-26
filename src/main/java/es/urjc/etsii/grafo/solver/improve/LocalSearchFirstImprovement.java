package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public class LocalSearchFirstImprovement<M extends Move<S,I>, S extends Solution<I>, I extends Instance> extends LocalSearch<M,S,I> {

    public LocalSearchFirstImprovement(MoveComparator<M,S,I> comparator, String lsType, Neighborhood<M,S, I>... ps){
        super(comparator, lsType, ps);
    }

    @Override
    protected M getMove(S s){
        M move = null;
        for (var provider : providers) {
            var optionalMove = provider.stream(s).filter(Move::isValid).filter(Move::improves).findAny();
            if(optionalMove.isEmpty()) continue;
            M _move = optionalMove.get();
            if (move == null) {
                move = _move;
            } else {
                move = comparator.getBest(move, _move);
            }
        }
        return move;
    }


    @Override
    public String toString() {
        return "LSFirst{type=" + lsType + "}";
    }
}
