package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.List;

public class TSPTWVND extends Improver<TSPTWSolution, TSPTWInstance> {
    private final List<TSPTWCostSearch> searches;

    @AutoconfigConstructor
    public TSPTWVND(@ComponentParam(min = 1, max = 2) List<TSPTWCostSearch> searches) {
        super(Main.OBJECTIVE);
        this.searches = TSPTWUtil.distinctComponents(searches, 1, 2);
    }

    public TSPTWVND() {
        this(List.of(new TSPTWInsertionSearch(), new TSPTWTwoOptSearch()));
    }

    @Override
    public TSPTWSolution improve(TSPTWSolution solution) {
        if (solution.constraint_violations() != 0) {
            throw new IllegalArgumentException("VND requires a feasible TSPTW tour");
        }
        boolean repeat;
        do {
            repeat = false;
            for (int i = 0; i < searches.size() && !TimeControl.isTimeUp(); i++) {
                double before = solution.cost();
                solution = searches.get(i).improve(solution);
                // The first search is already exhausted. Only a later search can enable it again.
                if (i > 0 && solution.cost() < before) repeat = true;
            }
        } while (repeat && !TimeControl.isTimeUp());
        return solution;
    }

    @Override
    public String toString() {
        return "TSPTWVND{searches=" + searches + "}";
    }
}
