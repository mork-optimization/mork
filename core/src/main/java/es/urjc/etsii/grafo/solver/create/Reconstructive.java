package es.urjc.etsii.grafo.solver.create;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Represents a constructive method that can rebuild solutions after they have been partially destroyed,
 * or any solution in general that it is not fully constructed.
 * An example use case is in IteratedGreedy and similar algorithms, applied after the destruction phase.
 * If your method does not work if the solution given as a parameter can be partially built, do NOT extend this class,
 * and use Constructive instead.
 * @see es.urjc.etsii.grafo.solver.create.Constructive
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Reconstructive<S extends Solution<S,I>, I extends Instance> extends Constructive<S,I> {

    /**
     * Rebuild a partially assigned / partially destroyed solution
     * @param solution solution partially built
     * @return valid solution completely built
     */
    public abstract S reconstruct(S solution);
}
