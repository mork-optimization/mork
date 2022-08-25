package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.services.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract IteratedImprover class.</p>
 *
 */
public abstract class IteratedImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {
    static final Logger log = LoggerFactory.getLogger(IteratedImprover.class);
    static final int WARN_LIMIT = 10_000;

    /**
     * {@inheritDoc}
     *
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current solution any further
     */
    @Override
    protected S _improve(S solution) {
        int rounds = 0;
        while (!Global.stop() && iteration(solution)){
            log.debug("Executing iteration {} for {}", rounds, this.getClass().getSimpleName());
            rounds++;
            if(rounds == WARN_LIMIT){
                log.warn("Too many iterations, soft limit of {} passed, maybe {} is stuck in an infinite loop?", WARN_LIMIT, this.getClass().getSimpleName());
            }
        }
        log.debug("Improvement ended. {} executed {} iterations.", this.getClass().getSimpleName(), rounds);
        return solution;
    }

    /**
     * Tries to improve the received solution
     *
     * @param solution Solution to improve
     * @return True if the solution has been improved, false otherwise
     */
    public abstract boolean iteration(S solution);
}
