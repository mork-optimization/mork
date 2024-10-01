package es.urjc.etsii.grafo.improve.sa;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;

/**
 * Default termination criteria based on metropolis exponential function
 */
public class MetropolisAcceptanceCriteria<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> implements AcceptanceCriteria<M, S, I>{

    private final Objective<M, S, I> objective;

    public MetropolisAcceptanceCriteria(Objective<M, S, I> objective) {
        this.objective = objective;
    }

    @Override
    public boolean accept(M move, double currentTemperature) {
        double change = Math.abs(objective.evalMove(move));
        double metropolis = Math.exp(- change / currentTemperature);
        double roll = RandomManager.getRandom().nextDouble();
        return roll < metropolis;
    }
}
