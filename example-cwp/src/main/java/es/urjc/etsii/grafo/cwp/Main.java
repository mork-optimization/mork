package es.urjc.etsii.grafo.cwp;

import es.urjc.etsii.grafo.cwp.model.CWPBaseMove;
import es.urjc.etsii.grafo.cwp.model.CWPInstance;
import es.urjc.etsii.grafo.cwp.model.CWPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    /** CWP is a minimization problem: minimize the total client-to-hub distance. */
    public static final Objective<CWPBaseMove, CWPSolution, CWPInstance> MINIMIZE_WEIGHT =
            Objective.ofMinimizing("Weight", CWPSolution::getScore, CWPBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, MINIMIZE_WEIGHT);
    }
}
