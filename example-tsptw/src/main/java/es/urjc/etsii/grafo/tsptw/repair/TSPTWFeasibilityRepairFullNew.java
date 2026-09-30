package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

/** The original four repair phases in their fixed order, exposed as one parameterless component. */
public class TSPTWFeasibilityRepairFullNew extends TSPTWFeasibilityRepair {
    @AutoconfigConstructor
    public TSPTWFeasibilityRepairFullNew() {
        super();
    }

    @Override
    public String toString() {
        return "TSPTWFeasibilityRepairFullNew";
    }
}
