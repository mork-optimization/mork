package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.services.MorkLifecycle;

import java.util.logging.Logger;

public abstract class Improver<S extends Solution<I>,I extends Instance> {
    static final Logger log = Logger.getLogger(Improver.class.getName());
    
    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     * @param s model.Solution to improve
     * @return Improved s
     */
    public S improve(S s) {
        int rounds = 0;
        while (!MorkLifecycle.stop() && iteration(s)){
            rounds++;
        }
        log.fine(String.format("LS: %s executed %s iterations", this.getClass().getSimpleName(), rounds));
        return s;
    }

    /**
     * Tries to improve the recieved es.urjc.etsii.grafo.solution
     * @param s Solution to improve
     * @return True if the es.urjc.etsii.grafo.solution has been improved, false otherwise
     */
    public abstract boolean iteration(S s);
}
