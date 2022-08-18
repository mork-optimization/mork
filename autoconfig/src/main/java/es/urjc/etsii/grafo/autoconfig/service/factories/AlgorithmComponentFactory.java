package es.urjc.etsii.grafo.autoconfig.service.factories;

import java.util.Map;

/**
 * Create an algorithm component from a given set of parameters
 */
@FunctionalInterface
public interface AlgorithmComponentFactory {
    /**
     * Create algorithm component, can be an algorithm, a constructive method, a local search, etc.
     * @param params algorithm parameters
     * @return component
     */
    Object create(Map<String, Object> params);

    // TODO declare which parameters are valid for the current factory, necessary for the future grammar step
}
