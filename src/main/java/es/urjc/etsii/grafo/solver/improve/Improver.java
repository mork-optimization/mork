package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.concurrent.TimeUnit;

public interface Improver<S extends Solution<I>,I extends Instance> {
    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     * @param s model.Solution to improve
     * @return Improved s
     */
    default S improve(S s, long quantity, TimeUnit time) {
        long stopTime = System.nanoTime() + time.toNanos(quantity);
        //int rounds = 0;
        while (System.nanoTime() < stopTime && iteration(s)){
            //rounds++;
        }
        return s;
    }

    /**
     * Improves a Solution "indefinitely", timeouts after 1 Day (timeout may change)
     * @param s
     * @return
     */
    default S improve(S s){
        return improve(s, 1, TimeUnit.DAYS);
    }

    /**
     * Tries to improve the recieved es.urjc.etsii.grafo.solution
     * @param s Solution to improve
     * @return True if the es.urjc.etsii.grafo.solution has been improved, false otherwise
     */
    boolean iteration(S s);
}
