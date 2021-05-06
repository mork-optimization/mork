package es.urjc.etsii.grafo.solver.destructor;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.RandomizableNeighborhood;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public abstract class RandomMoveShake<S extends Solution<I>, I extends Instance> extends Shake<S,I> {

    private static final Logger log = Logger.getLogger(RandomMoveShake.class.getName());

    RandomizableNeighborhood<?,S,I>[] neighborhoods;
    private int ratio;

    @SafeVarargs
    public RandomMoveShake(int ratio, RandomizableNeighborhood<?,S,I>... neighborhoods) {
        this.ratio = ratio;
        if(neighborhoods.length == 0){
            throw new IllegalArgumentException("Use at least one MoveProvider");
        }
        this.neighborhoods = neighborhoods;
    }

    @SafeVarargs
    public RandomMoveShake(RandomizableNeighborhood<?,S,I>... neighborhoods) {
        this(1, neighborhoods);
    }

    /**
     * Shake the solution applying random movements from the configured neighborhood
     * @param s Solution to shake
     * @param k Number of movements to apply, maxK is not used in this implementation
     */
    public S shake(S s, int k, int maxK, boolean inPlace) {
        if(!inPlace){
            s = s.cloneSolution();
        }
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
                assert DoubleComparator.equals(s.getScore(), s.recalculateScore()) :
                        String.format("Score mismatch, incremental %s, absolute %s. Review your incremental score calculation.", s.getScore(), s.recalculateScore());
            } else {
                log.warning("No move available in any of the given providers, ending Destruction phase now");
                break;
            }
        }
        repairSolution(s);
        return s;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "neighborhoods=" + Arrays.toString(this.neighborhoods) +
                '}';
    }

    /**
     * Repairs a solution after applying a set of random movements
     * If the solution does not need to be repaired, this method should be empty
     * @param s Solution to repair
     */
    protected abstract void repairSolution(S s);
}
