package es.urjc.etsii.grafo.tsp;

import es.urjc.etsii.grafo.tsp.model.TSPBaseMove;
import es.urjc.etsii.grafo.tsp.model.TSPInstance;
import es.urjc.etsii.grafo.tsp.model.TSPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<TSPBaseMove, TSPSolution, TSPInstance> MINIMIZE_DISTANCE
            = Objective.ofMinimizing("Distance", TSPSolution::getDistance, TSPBaseMove::getDistanceDelta);

    public static void main(String[] args) {
        Mork.start(args, MINIMIZE_DISTANCE);
    }
}
