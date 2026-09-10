package es.urjc.etsii.grafo.cph;

import es.urjc.etsii.grafo.cph.model.CPHBaseMove;
import es.urjc.etsii.grafo.cph.model.CPHInstance;
import es.urjc.etsii.grafo.cph.model.CPHSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    /** CPH is a minimization problem: minimize the total client-to-hub distance. */
    public static final Objective<CPHBaseMove, CPHSolution, CPHInstance> MINIMIZE_WEIGHT =
            Objective.ofMinimizing("Weight", CPHSolution::getScore, CPHBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, MINIMIZE_WEIGHT);
    }
}
