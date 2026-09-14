package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.util.TimeControl;

/** A cost neighborhood exhausted by first improvement; input and output must be feasible. */
public abstract class TSPTWCostSearch extends Improver<TSPTWSolution, TSPTWInstance> {
    protected TSPTWCostSearch() {
        super(Main.OBJECTIVE);
    }

    @Override
    public TSPTWSolution improve(TSPTWSolution solution) {
        if (solution.constraint_violations() != 0) {
            throw new IllegalArgumentException("Cost search requires a feasible TSPTW tour");
        }
        while (!TimeControl.isTimeUp() && improveOnce(solution)) {
            solution.notifyUpdate();
            solution.assert_solution();
            Metrics.addCurrentObjectives(solution);
        }
        return solution;
    }

    protected abstract boolean improveOnce(TSPTWSolution solution);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
