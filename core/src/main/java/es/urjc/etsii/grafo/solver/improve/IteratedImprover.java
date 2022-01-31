package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.services.Global;

import java.util.logging.Logger;

/**
 * <p>Abstract IteratedImprover class.</p>
 *
 */
public abstract class IteratedImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I>{
    static final Logger log = Logger.getLogger(IteratedImprover.class.getName());
    
    /**
     * {@inheritDoc}
     *
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     */
    @Override
    protected S _improve(S s) {
        int rounds = 0;
        while (!Global.stop() && iteration(s)){
            log.fine(String.format("Executing iteration %s for %s", rounds, this.getClass().getSimpleName()));
            rounds++;
        }
        log.fine(String.format("Improvement ended. %s executed %s iterations.", this.getClass().getSimpleName(), rounds));
        return s;
    }

    /**
     * Tries to improve the recieved es.urjc.etsii.grafo.solution
     *
     * @param s Solution to improve
     * @return True if the es.urjc.etsii.grafo.solution has been improved, false otherwise
     */
    public abstract boolean iteration(S s);
}
