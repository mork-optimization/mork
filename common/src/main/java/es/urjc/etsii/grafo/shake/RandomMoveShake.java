package es.urjc.etsii.grafo.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Shake a solution by executing a sequence of random moves
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class RandomMoveShake<S extends Solution<S,I>, I extends Instance> extends Shake<S,I> {

    private static final Logger log = LoggerFactory.getLogger(RandomMoveShake.class);

    /**
     * Set of randomizable neighborhoods available for the shake.
     * A move will be randomly selected from any neighborhood
     */
    RandomizableNeighborhood<?,S,I> neighborhood;
    private int ratio;

    /**
     * Create a new RandomMoveShake
     *
     * @param ratio number of moves to execute = ratio * K
     * @param neighborhood neighborhoods to use
     */
    @AutoconfigConstructor
    public RandomMoveShake(@IntegerParam(min = 1, max = 1000) int ratio, RandomizableNeighborhood<?,S,I> neighborhood) {
        this.ratio = ratio;
        this.neighborhood = Objects.requireNonNull(neighborhood);
    }

    /**
     * Create a new RandomMoveShake. Equivalent to RandomMoveShake(1, neighborhoods)
     *
     * @param neighborhood neighborhoods to use
     */
    public RandomMoveShake(RandomizableNeighborhood<?,S,I> neighborhood) {
        this(1, neighborhood);
    }

    /**
     * {@inheritDoc}
     *
     * Shake the solution applying random movements from the configured neighborhood
     */
    public S shake(S solution, int k) {
        // Execute k*RATIO random moves in the given neighborhood
        for (int i = 0; i < k*ratio; i++) {
            if(TimeControl.isTimeUp()){
                return solution;
            }
            var move = this.neighborhood.getRandomMove(solution);
            if(move.isPresent()){
                move.get().execute(solution);
                assert Context.validate(solution);
            } else {
                log.debug("No move available in {}, ending shake at {} of {} iterations", neighborhood.getClass().getSimpleName(), i, k*ratio);
                break;
            }
        }
        return solution;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "neighborhood=" + this.neighborhood +
                '}';
    }
}
