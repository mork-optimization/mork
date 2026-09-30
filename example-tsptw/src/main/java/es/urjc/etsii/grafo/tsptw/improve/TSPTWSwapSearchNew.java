package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** First-improvement exchange of any two customers, including nonadjacent positions. */
public class TSPTWSwapSearchNew extends TSPTWCostSearch {
    @AutoconfigConstructor
    public TSPTWSwapSearchNew() {}

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return TSPTWNewMoveUtil.improveSwap(solution);
    }
}
