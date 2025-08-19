package es.urjc.etsii.grafo.VRPOD;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.BaseMove;
import es.urjc.etsii.grafo.VRPOD.model.solution.ODToRouteMove;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<BaseMove, VRPODSolution, VRPODInstance> OBJ = Objective.ofMinimizing("Cost", VRPODSolution::getScore, BaseMove::getValue);

    public static void main(String[] args) {
        Mork.start(args, OBJ);
    }
}
