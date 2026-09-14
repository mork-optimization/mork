package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

public class TSPTWBackwardNonviolated extends TSPTWRepairPhase {
    @AutoconfigConstructor
    public TSPTWBackwardNonviolated() {
        super(true, false);
    }
}
