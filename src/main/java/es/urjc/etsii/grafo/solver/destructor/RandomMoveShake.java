package es.urjc.etsii.grafo.solver.destructor;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.RandomManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class RandomMoveShake<S extends Solution<I>, I extends Instance> implements Shake<S,I> {

    Neighborhood<S,I>[] neighborhoods;

    @SafeVarargs
    public RandomMoveShake(Neighborhood<S,I>... neighborhoods) {
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
    public void iteration(S s, double k) {
        Random random = RandomManager.getRandom();

        // Execute k random moves in different neighbourhoods
        for (int i = 0; i < k; i++) {
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
