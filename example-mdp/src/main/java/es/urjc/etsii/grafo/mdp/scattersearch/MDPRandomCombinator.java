package es.urjc.etsii.grafo.mdp.scattersearch;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Combines two reference-set solutions by taking the union of their selected nodes and then
 * discarding random nodes until exactly {@code m} remain. This is the "without information"
 * combination: it ignores node contributions, mirroring the random combinator of the hand-written
 * port. Used by {@code ScatterSearchBuilder.withCombinator(...)}.
 */
public class MDPRandomCombinator extends SolutionCombinator<MDPSolution, MDPInstance> {

    @Override
    protected List<MDPSolution> apply(MDPSolution left, MDPSolution right) {
        MDPInstance instance = left.getInstance();
        int target = instance.getNumSolutionNodes();

        Set<MDPNode> union = new LinkedHashSet<>(left.createNodeList());
        union.addAll(right.createNodeList());

        MDPSolution combined = new MDPSolution(new ArrayList<>(union), instance);

        List<MDPNode> present = combined.createNodeList();
        CollectionUtil.shuffle(present);
        int toRemove = combined.getNumNodes() - target;
        for (int i = 0; i < toRemove; i++) {
            combined.removeNode(present.get(i));
        }

        combined.notifyUpdate();
        return List.of(combined);
    }
}
