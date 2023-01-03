package es.urjc.etsii.grafo.autoconfig.fill;


import es.urjc.etsii.grafo.annotations.InheritedComponent;

/**
 * Provides parameter values at runtime
 */
@InheritedComponent
public abstract class ParameterProvider {

    /**
     * Is this provider able to generate values for the parameter of type "type" with parameter name "paramName"?
     * @see es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil::isAssignable
     * @param type Class or type for which the parameter is being generated.
     * @param paramName parameter name as declared in source code
     * @return true if this provider will generate the corresponding value, false otherwise
     */
    public abstract boolean provides(Class<?> type, String paramName);

    /**
     * Generate a value for the parameter of type "type" with parameter name "paramName"
     * @see es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil::isAssignable
     * @param type Class or type for which the parameter is being generated.
     * @param paramName parameter name as declared in source code
     * @return generated value
     */
    public abstract Object getValue(Class<?> type, String paramName);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
