package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * Provides the necessary methods to generate an algorithm from the proposed Irace configuration.
 * Implementation is mandatory only if irace integration is used.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@InheritedComponent
public abstract class AlgorithmBuilder<S extends Solution<S,I>, I extends Instance> {

    /**
     * Generates an algorithm from a given Irace config.
     * Seed, instance, and other common parameters are automatically processed by Mork
     *
     * @param config Subset of config parameters, only those required for algorithm configuration
     * @return Algorithm generated according to Irace chosen parameters
     */
    public abstract Algorithm<S,I> buildFromConfig(AlgorithmConfiguration config);

    /**
     * Build an algorithm from a config string such as those returned by irace.
     * Example: "constructive=random balanced=true initialmaxdiffratio=0.8193 cooldownexpratio=0.9438 cyclelength=9"
     * @param paramString config string with key-values for each parameter
     * @return built algorithm
     */
    public Algorithm<S,I> buildFromStringParams(String paramString){
        String[] params = paramString.split("\\s+");
        return buildFromConfig(new AlgorithmConfiguration(params));
    }
}
