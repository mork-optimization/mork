package es.urjc.etsii.grafo.__RNAME__;

import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__BaseMove;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    // Replace Objective1 with your objective name, possible examples are: "Distance", "Cost", etc.
    public static final String OBJECTIVE_NAME = "Objective1";

    public static void main(String[] args) {
        // TODO Configure your objectives
        Mork.start(args, Objective.ofMinimizing(OBJECTIVE_NAME, __RNAME__Solution::getScore, __RNAME__BaseMove::getScoreChange));
    }
}
