package solver.create;

import io.Instance;
import solution.ConstructiveNeighborhood;
import solution.Solution;
import util.RandomManager;

import java.util.Random;
import java.util.function.Supplier;

public class RandomConstructor extends Constructor implements SolutionFixer {

    protected final Supplier<? extends RuntimeException> NOT_ENOUGH_MOVES = () -> new RuntimeException("Solution is not in a valid state but we do not have any available moves");

    @Override
    public <S extends Solution> S construct(Instance i, SolutionBuilder builder, ConstructiveNeighborhood neighborhood) {
        return assignMissing(builder.initializeSolution(i), neighborhood);
    }

    @Override
    public <S extends Solution> S assignMissing(S s, ConstructiveNeighborhood neighborhood) {
        Random r = RandomManager.getRandom();
        while(!s.isValid()){
            var move = neighborhood.getRandomMove(s).orElseThrow(NOT_ENOUGH_MOVES);
            move.execute();
        }
        return s;
    }
}
