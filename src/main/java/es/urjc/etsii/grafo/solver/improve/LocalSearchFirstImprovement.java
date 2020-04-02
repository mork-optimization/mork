package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public class LocalSearchFirstImprovement<S extends Solution<I>, I extends Instance> extends LocalSearch<S,I> {

    public LocalSearchFirstImprovement(String lsType, Neighborhood<S, I>... ps){
        super(lsType, ps);
    }

    @Override
    protected Move<S,I> getMove(S s){
        Move<S,I> move = null;
        for (var provider : providers) {
            var optionalMove = provider.stream(s).filter(Move::isValid).filter(Move::improves).findAny();
            if(optionalMove.isEmpty()) continue;
            Move<S,I> _move = optionalMove.get();
            if (move == null) {
                move = _move;
            } else {
                move = move.getBestMove(_move);
            }
        }
        return move;
    }


    @Override
    public String toString() {
        return "LSFirst{type=" + lsType + "}";
    }
}
