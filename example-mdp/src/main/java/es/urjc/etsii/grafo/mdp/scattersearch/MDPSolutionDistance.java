package es.urjc.etsii.grafo.mdp.scattersearch;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionDistance;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;

/**
 * Distance between two MDP solutions, used by the Scatter Search to build a diverse reference set.
 * Both solutions select exactly {@code m} nodes, so the number of nodes present in one but not the
 * other is {@code m − |A ∩ B|}; this is symmetric, satisfying {@code distance(a,b) == distance(b,a)}.
 */
public class MDPSolutionDistance extends SolutionDistance<MDPSolution, MDPInstance> {

    @Override
    public double distances(MDPSolution sa, MDPSolution sb) {
        int shared = 0;
        for (MDPNode node : sa.createNodeList()) {
            if (sb.contains(node)) {
                shared++;
            }
        }
        return sa.getNumNodes() - shared;
    }
}
