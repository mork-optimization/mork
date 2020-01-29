package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public class LocalSearchBestImprovement extends LocalSearch {

    public LocalSearchBestImprovement(String lsType, Neighborhood... ps){
        super(lsType, ps);
    }

    @Override
    protected Move getMove(Solution s){
        Move move = null;
        for (var provider : providers) {
            var _move = provider.stream(s).reduce((a, b) -> b.getBestMove(a));
            if(_move.isEmpty()) continue;
            if (move == null) {
                move = _move.get();
            } else {
                move = move.getBestMove(_move.get());
            }
        }
        return move;
    }


    @Override
    public String toString() {
        return "LSBest{type=" + lsType + "}";
    }
}
