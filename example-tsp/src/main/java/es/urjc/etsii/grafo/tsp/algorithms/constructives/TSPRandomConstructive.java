package es.urjc.etsii.grafo.tsp.algorithms.constructives;

import es.urjc.etsii.grafo.tsp.model.TSPInstance;
import es.urjc.etsii.grafo.tsp.model.TSPSolution;
import es.urjc.etsii.grafo.create.Constructive;

public class TSPRandomConstructive extends Constructive<TSPSolution, TSPInstance> {

    @Override
    public TSPSolution construct(TSPSolution solution) {
        for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {
            solution.setOrderOfLocation(i, i);
        }
        solution.shuffleRoute();
        solution.setScore(solution.recalculateScore());
        solution.notifyUpdate();
        return solution;
    }
}
