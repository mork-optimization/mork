package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

public class TSPTWInsertionSearch extends TSPTWCostSearch {
    @AutoconfigConstructor
    public TSPTWInsertionSearch() {}

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return solution.feasible_1shift_first();
    }
}
