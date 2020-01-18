package solver.destructor;

import solution.Move;
import solution.Neighborhood;
import solution.Solution;
import util.RandomManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class RandomMoveShake implements Shake {

    Neighborhood[] neighborhoods;

    public RandomMoveShake(Neighborhood... neighborhoods) {
        if(neighborhoods.length == 0){
            throw new IllegalArgumentException("Use at least one MoveProvider");
        }
        this.neighborhoods = neighborhoods;
    }

    /**
     *
     * @param s
     * @param k K >= 1 and K<= 20
     */
    public void iteration(Solution s, int k) {
        Random random = RandomManager.getRandom();

        // Execute k random moves in different neighbourhoods
        for (int i = 0; i < k; i++) {
            int chosenNeigh = random.nextInt(neighborhoods.length);
            int copy = chosenNeigh;
            Optional<Move> move;
            do {
                move = neighborhoods[chosenNeigh % neighborhoods.length].getRandomMove(s);
                if (move.isPresent()) {
                    break;
                }
                chosenNeigh++;
            } while (chosenNeigh % neighborhoods.length != copy);
            if(move.isPresent()){
                move.get().execute();
                // TODO change the way validation is triggered
                s.getOptimalValue(); // Trigger validation
            } else {
                System.out.println("WARNING: No move available in any of the given providers, ending Destruction phase now");
                break;
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "neighborhoods=" + Arrays.toString(this.neighborhoods) +
                '}';
    }
}
