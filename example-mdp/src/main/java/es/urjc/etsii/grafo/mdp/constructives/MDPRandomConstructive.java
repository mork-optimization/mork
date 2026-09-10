package es.urjc.etsii.grafo.mdp.constructives;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.List;

/**
 * Builds a random MDP solution. Mork hands the constructive a fresh solution that already contains
 * <b>all</b> the instance nodes (the reflective builder calls {@code new MDPSolution(instance)}), so
 * the constructive reduces it to exactly {@code m} nodes by removing {@code n − m} random ones.
 *
 * <p>It is used both as the "diverse" constructive of the Scatter Search and, in the random
 * configuration, as the "good value" constructive too.
 */
public class MDPRandomConstructive extends Constructive<MDPSolution, MDPInstance> {

    @Override
    public MDPSolution construct(MDPSolution solution) {
        int target = solution.getInstance().getNumSolutionNodes();

        List<MDPNode> present = solution.createNodeList();
        CollectionUtil.shuffle(present);

        int toRemove = solution.getNumNodes() - target;
        for (int i = 0; i < toRemove; i++) {
            solution.removeNode(present.get(i));
        }

        solution.notifyUpdate();
        return solution;
    }
}
