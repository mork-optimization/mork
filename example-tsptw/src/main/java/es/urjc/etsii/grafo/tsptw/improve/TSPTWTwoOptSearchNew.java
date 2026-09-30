package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** Scratch-evaluation variant of TSPTWTwoOptSearch, preserving its candidate order and pruning. */
public class TSPTWTwoOptSearchNew extends TSPTWCostSearch {
    @AutoconfigConstructor
    public TSPTWTwoOptSearchNew() {}

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return TSPTWNewMoveUtil.improveTwoOpt(solution);
    }
}
