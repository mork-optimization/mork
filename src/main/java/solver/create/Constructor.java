package solver.create;

import io.Instance;
import solution.ConstructiveNeighborhood;
import solution.Solution;

public abstract class Constructor {

    /**
     * Initialize a solution using any of the available strategies
     * @param builder Specify how a solution is initialized from an instance
     * @return A valid solution that fulfills all the problem constraints
     */
    abstract <S extends Solution> S construct(Instance i, SolutionBuilder builder, ConstructiveNeighborhood neighborhood);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
