package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.util.Optional;

/**
 * If implemented, used to obtain a % dev to the best value known, and calculate number of best values accordingly
 */
@InheritedComponent
public abstract class ReferenceResultProvider {
    /**
     * Get reference f.o value for a given instance
     * @param instanceName Instance name, as in Instance::getName
     * @return reference objective value for the given instance
     */
    public abstract Optional<Double> getValueFor(String instanceName);
}
