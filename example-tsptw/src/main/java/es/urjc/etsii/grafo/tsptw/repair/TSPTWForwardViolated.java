package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class TSPTWForwardViolated extends TSPTWRepairPhase {
    @AutoconfigConstructor
    public TSPTWForwardViolated() {
        super(false, true);
    }
}
