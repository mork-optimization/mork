package es.urjc.etsii.grafo.vrpod;

import es.urjc.etsii.grafo.vrpod.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.vrpod.model.solution.BaseMove;
import es.urjc.etsii.grafo.vrpod.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<BaseMove, VRPODSolution, VRPODInstance> OBJ = Objective.ofMinimizing("Cost", VRPODSolution::getScore, BaseMove::getValue);

    public static void main(String[] args) {
        Mork.start(args, OBJ);
    }
}
