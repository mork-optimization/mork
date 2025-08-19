package es.urjc.etsii.grafo.CAP;

import es.urjc.etsii.grafo.CAP.model.CAPBaseMove;
import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;


public class Main {
    public static final Objective<CAPBaseMove, CAPSolution, CAPInstance> OBJ = Objective.ofMinimizing("Score", CAPSolution::getScore, CAPBaseMove::getValue);

    public static void main(String[] args) {
        Mork.start(args, OBJ);
    }
}
