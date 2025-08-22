package es.urjc.etsii.grafo.TSPTW;

import es.urjc.etsii.grafo.TSPTW.model.TSPTWBaseMove;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    // TODO Configure your objective
    // Replace Objective1 with your objective name, possible examples are: "Distance", "Cost", etc.
    // If maximizing, replace ofMinimizing with ofMaximizing
    public static final Objective<TSPTWBaseMove, TSPTWSolution, TSPTWInstance> OBJECTIVE =
            Objective.ofMinimizing("Objective1", TSPTWSolution::getScore, TSPTWBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);

        // For a more flexible approach, you can use the following method:
        // Mork.start(args, multiobjective:true/false, myObjective1, myObjective2, myObjective3);
        // See the docs for more information on how to configure your objectives if using alternative objective functions
        // or you are solving a multi-objective problem
    }
}
