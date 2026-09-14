package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class TSPTWForwardNonviolated extends TSPTWRepairPhase {
    @AutoconfigConstructor
    public TSPTWForwardNonviolated() {
        super(false, false);
    }
}
