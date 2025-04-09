package es.urjc.etsii.grafo.__RNAME__;

import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__BaseMove;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    // TODO Configure your objective
    // Replace Objective1 with your objective name, possible examples are: "Distance", "Cost", etc.
    // If maximizing, replace ofMinimizing with ofMaximizing
    public static final Objective<__RNAME__BaseMove, __RNAME__Solution, __RNAME__Instance> OBJECTIVE =
            Objective.ofMinimizing("Objective1", __RNAME__Solution::getScore, __RNAME__BaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);

        // For a more flexible approach, you can use the following method:
        // Mork.start(args, multiobjective:true/false, myObjective1, myObjective2, myObjective3);
        // See the docs for more information on how to configure your objectives if using alternative objective functions
        // or you are solving a multi-objective problem
    }
}
