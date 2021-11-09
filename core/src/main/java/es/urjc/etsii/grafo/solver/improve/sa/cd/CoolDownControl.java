package es.urjc.etsii.grafo.solver.improve.sa.cd;


import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Given current iteration and temperature set next iteration temperature
 */
@FunctionalInterface
public interface CoolDownControl<M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> {
    double coolDown(S solution, Neighborhood<M, S, I> neighborhood, double currentTemperature, int iteration);
}
