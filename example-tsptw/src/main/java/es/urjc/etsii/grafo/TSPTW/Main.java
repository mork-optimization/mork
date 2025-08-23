package es.urjc.etsii.grafo.TSPTW;

import es.urjc.etsii.grafo.TSPTW.model.TSPTWBaseMove;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    public static final Objective<TSPTWBaseMove, TSPTWSolution, TSPTWInstance> OBJECTIVE =
            Objective.ofMinimizing("Cost", TSPTWSolution::getScore, TSPTWBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);
    }
}
