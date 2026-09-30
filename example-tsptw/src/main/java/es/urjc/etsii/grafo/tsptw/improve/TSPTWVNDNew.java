package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;

/** Three-neighborhood variant of TSPTWVND, restarting earlier searches after a later improvement. */
public class TSPTWVNDNew extends TSPTWVND {
    private final List<TSPTWCostSearch> searches;

    @AutoconfigConstructor
    public TSPTWVNDNew(@ComponentParam(min = 3, max = 3) List<TSPTWCostSearch> searches) {
        this.searches = TSPTWUtil.distinctComponents(searches, 3, 3);
    }

    public TSPTWVNDNew() {
        this(List.of(new TSPTWInsertionSearchNew(), new TSPTWTwoOptSearchNew(), new TSPTWOrOptSearchNew()));
    }

    @Override
    public TSPTWSolution improve(TSPTWSolution solution) {
        TSPTWNewMoveUtil.requireFeasible(solution);
        int index = 0;
        while (index < searches.size() && !TimeControl.isTimeUp()) {
            double before = solution.cost();
            solution = searches.get(index).improve(solution);
            if (index > 0 && solution.cost() < before) index = 0;
            else index++;
        }
        return solution;
    }

    @Override
    public String toString() { return "TSPTWVNDNew{searches=" + searches + "}"; }
}
