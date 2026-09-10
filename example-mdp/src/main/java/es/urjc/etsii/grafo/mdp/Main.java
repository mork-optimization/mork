package es.urjc.etsii.grafo.mdp;

import es.urjc.etsii.grafo.mdp.model.MDPBaseMove;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    /** MDP is a maximization problem: maximize the total diversity (sum of pairwise distances). */
    public static final Objective<MDPBaseMove, MDPSolution, MDPInstance> MAXIMIZE_DIVERSITY =
            Objective.ofMaximizing("Diversity", MDPSolution::getWeight, MDPBaseMove::getScoreChange);

    public static void main(String[] args) {
        Mork.start(args, MAXIMIZE_DIVERSITY);
    }
}
