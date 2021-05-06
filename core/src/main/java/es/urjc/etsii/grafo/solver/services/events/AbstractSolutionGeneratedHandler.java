package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

/**
 * Each time an algorithm finishes executing, a SolutionGeneratedEvent is triggered
 * Extend this class to get notified when a solution is considered finished and add custom behaviour without affecting the solution timings
 * Ex: Saving objective function value for each iteration and see how it evolves over time.
 */
@InheritedComponent
public abstract class AbstractSolutionGeneratedHandler<S extends Solution<I>, I extends Instance> {

    /**
     * This method will be executed each time an algorithm
     * @param event Event triggered when a new solution has been generated (no further changes will be made to it)
     */
    public abstract void onSolutionGenerated(SolutionGeneratedEvent<S,I> event);
}
