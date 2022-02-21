package es.urjc.etsii.grafo.solver.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;

/**
 * Default termination criteria based on metropolis exponential function
 */
public class DefaultAcceptanceCriteria<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements AcceptanceCriteria<M, S, I>{
    @Override
    public boolean accept(M move, double currentTemperature) {
        double change = Math.abs(move.getValue());
        double metropolis = Math.exp(- change / currentTemperature);
        double roll = RandomManager.getRandom().nextDouble();
        return roll < metropolis;
    }
}
