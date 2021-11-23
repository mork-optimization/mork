package es.urjc.etsii.grafo.solver.irace;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

/**
 * Provides the necessary methods to generate an algorithm from the proposed Irace configuration.
 * Implementation is mandatory only if irace integration is used.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class IraceAlgorithmGenerator<S extends Solution<S,I>, I extends Instance> {

    /**
     * Generates an algorithm from a given Irace config.
     * Seed, instance, and other common parameters are automatically processed by Mork
     *
     * @param config Subset of config parameters, only those required for algorithm configuration
     * @return Algorithm generated according to Irace chosen parameters
     */
    public abstract Algorithm<S,I> buildAlgorithm(IraceRuntimeConfiguration config);

}
