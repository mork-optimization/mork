package solution;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class Neighborhood {

    /**
     * Build a stream that allows iterating the whole neighborhood
     * @return Stream with all the available moves in the neighborhood
     */
    public abstract Stream<? extends Move> stream(Solution s);

    /**
     * Pick a random movement within the neighborhood
     * @return a random move, if there is at least one valid move
     */
    public abstract Optional<Move> getRandomMovement(Solution s);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
