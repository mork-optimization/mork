package es.urjc.etsii.grafo.BMSSC;

import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.BMSSC.model.sol.BMSSCMove;
import es.urjc.etsii.grafo.BMSSC.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<BMSSCMove, BMSSCSolution, BMSSCInstance> OBJ = Objective.ofMinimizing("Cost", BMSSCSolution::getScore, BMSSCMove::getValue);

    public static void main(String[] args) {
        Mork.start(args, OBJ);
    }
}
