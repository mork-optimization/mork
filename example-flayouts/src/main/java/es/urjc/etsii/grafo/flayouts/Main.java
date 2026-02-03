package es.urjc.etsii.grafo.flayouts;

import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPMove;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    public static final Objective<FLPMove, FLPSolution, FLPInstance> FLOW = Objective.ofMinimizing("Flow", FLPSolution::getScore, FLPMove::delta);

    public static void main(String[] args) {
        Mork.start(args, FLOW);
    }
}
