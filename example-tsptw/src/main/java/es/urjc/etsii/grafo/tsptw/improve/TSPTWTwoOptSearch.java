package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

public class TSPTWTwoOptSearch extends TSPTWCostSearch {
    @AutoconfigConstructor
    public TSPTWTwoOptSearch() {}

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return solution.two_opt_first();
    }
}
