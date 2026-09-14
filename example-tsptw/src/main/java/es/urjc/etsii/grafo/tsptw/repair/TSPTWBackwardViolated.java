package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class TSPTWBackwardViolated extends TSPTWRepairPhase {
    @AutoconfigConstructor
    public TSPTWBackwardViolated() {
        super(true, true);
    }
}
