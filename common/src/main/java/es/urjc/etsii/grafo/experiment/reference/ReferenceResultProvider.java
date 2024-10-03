package es.urjc.etsii.grafo.experiment.reference;

import es.urjc.etsii.grafo.annotations.InheritedComponent;

/**
 * If implemented, used when comparing experiment results
 */
@InheritedComponent
public abstract class ReferenceResultProvider {

    /**
     * Empty reference result to be used when the given provider does not have a value for an instance,
     * and it is valid to have instances without reference values
     */
    public static final ReferenceResult EMPTY_REFERENCE_RESULT = new ReferenceResult();

    /**
     * Get reference f.o value for a given instance
     *
     * @param instanceName Instance name, as in Instance::getName
     * @return reference objective value for the given instance. If the instance is not solved by the given algorithm,
     * return an empty ReferenceResult
     */
    public abstract ReferenceResult getValueFor(String instanceName);

    /**
     * How would you call the source of this data? A previous paper? An exact solver?.
     * Examples: Gurobi, SOTA, PacoEtAl, etc.
     *
     * @return Name for this Provider
     */
    public abstract String getProviderName();

    @Override
    public String toString() {
        return this.getProviderName();
    }
}


