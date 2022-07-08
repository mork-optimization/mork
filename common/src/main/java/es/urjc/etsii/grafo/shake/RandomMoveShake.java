package es.urjc.etsii.grafo.shake;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.ValidationUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Shake a solution by executing a sequence of random moves
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class RandomMoveShake<S extends Solution<S,I>, I extends Instance> extends Shake<S,I> {

    private static final Logger log = Logger.getLogger(RandomMoveShake.class.getName());

    /**
     * Set of randomizable neighborhoods available for the shake.
     * A move will be randomly selected from any neighborhood
     */
    RandomizableNeighborhood<?,S,I>[] neighborhoods;
    private int ratio;

    /**
     * Create a new RandomMoveShake
     *
     * @param ratio number of moves to execute = ratio * K
     * @param neighborhoods neighborhoods to use
     */
    @SafeVarargs
    public RandomMoveShake(int ratio, RandomizableNeighborhood<?,S,I>... neighborhoods) {
        this.ratio = ratio;
        if(neighborhoods.length == 0){
            throw new IllegalArgumentException("Use at least one MoveProvider");
        }
        this.neighborhoods = neighborhoods;
    }

    /**
     * Create a new RandomMoveShake. Equivalent to RandomMoveShake(1, neighborhoods)
     *
     * @param neighborhoods neighborhoods to use
     */
    @SafeVarargs
    public RandomMoveShake(RandomizableNeighborhood<?,S,I>... neighborhoods) {
        this(1, neighborhoods);
    }

    /**
     * {@inheritDoc}
     *
     * Shake the solution applying random movements from the configured neighborhood
     */
    public S shake(S solution, int k) {
        var random = RandomManager.getRandom();

        // Execute k*RATIO random moves in different neighbourhoods
        for (int i = 0; i < k*ratio; i++) {
            int chosenNeigh = random.nextInt(neighborhoods.length);
            int copy = chosenNeigh;
            Optional<? extends Move<S,I>> move;
            do {
                move = neighborhoods[chosenNeigh % neighborhoods.length].getRandomMove(solution);
                if (move.isPresent()) {
                    break;
                }
                chosenNeigh++;
            } while (chosenNeigh % neighborhoods.length != copy);
            if(move.isPresent()){
                move.get().execute(solution);
                ValidationUtil.assertValidScore(solution);
            } else {
                log.warning("No move available in any of the given providers, ending Destruction phase now");
                break;
            }
        }
        repairSolution(solution);
        return solution;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "neighborhoods=" + Arrays.toString(this.neighborhoods) +
                '}';
    }

    /**
     * Repairs a solution after applying a set of random movements
     * If the solution does not need to be repaired, this method should be empty
     *
     * @param solution Solution to repair
     */
    protected void repairSolution(S solution){

    }
}
