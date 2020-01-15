package solution;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines a neighbourhood for the local search methods
 */
public abstract class Neighborhood {

    /**
     * Build an exhaustive stream that allows iterating the whole neighborhood
     * Using a stream is more efficient that a list
     * as moves are only generated if they are needed
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<? extends Move> stream(Solution s);

    /**
     * Pick a random move within the neighborhood
     * @return a random move, if there is at least one valid move
     */
    public abstract Optional<Move> getRandomMove(Solution s);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
