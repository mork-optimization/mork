package es.urjc.etsii.grafo.mdp.scattersearch;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.util.Weighted;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Combines two reference-set solutions by taking the union of their selected nodes and then
 * greedily removing the node that contributes <b>least</b> to the total diversity until exactly
 * {@code m} remain. This is the "with information" (D2) combination, mirroring the D2 combinator of
 * the hand-written port. Used by {@code ScatterSearchBuilder.withCombinator(...)}.
 */
public class MDPD2Combinator extends SolutionCombinator<MDPSolution, MDPInstance> {

    @Override
    protected List<MDPSolution> apply(MDPSolution left, MDPSolution right) {
        MDPInstance instance = left.getInstance();
        int target = instance.getNumSolutionNodes();

        Set<MDPNode> union = new LinkedHashSet<>(left.createNodeList());
        union.addAll(right.createNodeList());

        MDPSolution combined = new MDPSolution(new ArrayList<>(union), instance);

        while (combined.getNumNodes() > target) {
            Weighted<MDPNode> worst = null;
            for (Weighted<MDPNode> w : combined.getNodesDistance()) {
                if (worst == null || w.getWeight() < worst.getWeight()) {
                    worst = w;
                }
            }
            combined.removeNode(worst.getElement());
        }

        combined.notifyUpdate();
        return List.of(combined);
    }
}
