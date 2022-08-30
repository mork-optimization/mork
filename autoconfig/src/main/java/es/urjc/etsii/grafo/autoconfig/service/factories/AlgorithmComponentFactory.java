package es.urjc.etsii.grafo.autoconfig.service.factories;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;

import java.util.List;
import java.util.Map;

/**
 * Create an algorithm component from a given set of parameters
 */
@InheritedComponent
public abstract class AlgorithmComponentFactory {

    /**
     * Create algorithm component, can be an algorithm, a constructive method, a local search, etc.
     * @param params algorithm parameters
     * @return component
     */
    public abstract Object buildComponent(Map<String, Object> params);

    /**
     * Which are the parameters required by this factory?
     * @return
     */
    public abstract List<ComponentParameter> getRequiredParameters();

    /**
     * Which class type does this factory produce?
     * @return class reference, for example "return SimpleAlgorithm.class"
     */
    public abstract Class<?> produces();
}
