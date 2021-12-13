package es.urjc.etsii.grafo.solver.services.reference;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

/**
 * If implemented, used when comparing experiment results
 */
@InheritedComponent
public abstract class ReferenceResultProvider {

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
}


