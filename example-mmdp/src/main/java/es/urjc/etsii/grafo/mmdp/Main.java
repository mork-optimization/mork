package es.urjc.etsii.grafo.mmdp;

import es.urjc.etsii.grafo.mmdp.model.MMDPBaseMove;
import es.urjc.etsii.grafo.mmdp.model.MMDPInstance;
import es.urjc.etsii.grafo.mmdp.model.MMDPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    /** MMDP is a maximization problem: maximize the minimum pairwise distance of the selected nodes. */
    public static final Objective<MMDPBaseMove, MMDPSolution, MMDPInstance> MAXIMIZE_DIVERSITY =
            Objective.ofMaximizing("Diversity", MMDPSolution::getScore, MMDPBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, MAXIMIZE_DIVERSITY);
    }
}
