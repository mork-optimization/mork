package es.urjc.etsii.grafo.flayouts;

import es.urjc.etsii.grafo.flayouts.model.CAPBaseMove;
import es.urjc.etsii.grafo.flayouts.model.CAPInstance;
import es.urjc.etsii.grafo.flayouts.model.CAPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;


public class Main {
    public static final Objective<CAPBaseMove, CAPSolution, CAPInstance> OBJ = Objective.ofMinimizing("Score", CAPSolution::getScore, CAPBaseMove::getValue);

    public static void main(String[] args) {
        Mork.start(args, OBJ);
    }
}
