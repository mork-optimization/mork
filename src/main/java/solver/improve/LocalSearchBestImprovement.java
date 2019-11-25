package solver.improve;

import solution.Move;
import solution.Neighborhood;
import solution.Solution;

public class LocalSearchBestImprovement extends LocalSearch {

    public LocalSearchBestImprovement(String lsType, Neighborhood... ps){
        super(lsType, ps);
    }

    @Override
    protected Move getMovement(Solution s){
        Move move = null;
        for (var provider : providers) {
            var _move = provider.stream(s).min(Move::compareTo);
            if(_move.isEmpty()) continue;
            if (move == null || _move.get().compareTo(move) > 0) // >, we want bigger things
                move = _move.get();
        }
        return move;
    }


    @Override
    public String toString() {
        return "LSBest{type=" + lsType + "}";
    }
}
