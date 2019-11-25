package solver.improve;

import solution.Move;
import solution.Neighborhood;
import solution.Solution;

public class LocalSearchFirstImprovement extends LocalSearch {

    public LocalSearchFirstImprovement(String lsType, Neighborhood... ps){
        super(lsType, ps);
    }

    @Override
    protected Move getMovement(Solution s){
        Move move = null;
        for (var provider : providers) {
            var optionalMove = provider.stream(s).filter(Move::improves).findAny();
            if(optionalMove.isEmpty()) continue;
            Move _move = optionalMove.get();
            if (move == null || _move.compareTo(move) > 0) // >, we want bigger things
                move = _move;
        }
        return move;
    }


    @Override
    public String toString() {
        return "LSFirst{type=" + lsType + "}";
    }
}
