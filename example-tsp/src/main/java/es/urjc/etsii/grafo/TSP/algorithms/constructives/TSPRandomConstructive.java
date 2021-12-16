package es.urjc.etsii.grafo.TSP.algorithms.constructives;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solver.create.Constructive;

public class TSPRandomConstructive extends Constructive<TSPSolution, TSPInstance> {

    @Override
    public TSPSolution construct(TSPSolution solution) {
        for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {
            solution.setOrderOfLocation(i, i);
        }
        solution.shuffleRoute();
        solution.updateLastModifiedTime();
        solution.setScore(solution.recalculateScore());
        return solution;
    }
}
