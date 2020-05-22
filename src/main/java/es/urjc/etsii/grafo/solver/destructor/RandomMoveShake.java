package es.urjc.etsii.grafo.solver.destructor;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public class RandomMoveShake<S extends Solution<I>, I extends Instance> implements Shake<S,I> {

    private static final Logger log = Logger.getLogger(RandomMoveShake.class.getName());

    Neighborhood<S,I>[] neighborhoods;
    private int ratio;

    @SafeVarargs
    public RandomMoveShake(int ratio, Neighborhood<S,I>... neighborhoods) {
        this.ratio = ratio;
        if(neighborhoods.length == 0){
            throw new IllegalArgumentException("Use at least one MoveProvider");
        }
        this.neighborhoods = neighborhoods;
    }

    /**
     * Shake the solution applying random movements from the configured neighborhood
     * @param s Solution to shake
     * @param k Number of movements to apply, maxK is not used in this implementation
     */
    public void shake(S s, int k, int maxK) {
        Random random = RandomManager.getRandom();

        // Execute k*RATIO random moves in different neighbourhoods
        for (int i = 0; i < k*ratio; i++) {
            int chosenNeigh = random.nextInt(neighborhoods.length);
            int copy = chosenNeigh;
            Optional<? extends Move<S,I>> move;
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
                log.warning("No move available in any of the given providers, ending Destruction phase now");
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
