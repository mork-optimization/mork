package es.urjc.etsii.grafo.mdp.scattersearch;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Memory-guided combinator of the WITH_MEMORY Scatter Search variant. It unions the selected nodes
 * of two reference solutions and hands them to the shared {@link TabuD2Calculator}, which trims back
 * to {@code m} using the Tabu/D2 contribution-plus-ponderation criterion and updates the long-term
 * memory. Because it shares the {@link TabuD2Memory} with the constructive, construction and
 * combination feed and consult the same frequency/quality memory.
 */
public class TabuD2Combinator extends SolutionCombinator<MDPSolution, MDPInstance> {

    private final TabuD2Memory memory;

    public TabuD2Combinator(TabuD2Memory memory) {
        this.memory = memory;
    }

    @Override
    protected List<MDPSolution> apply(MDPSolution left, MDPSolution right) {
        Set<MDPNode> union = new HashSet<>(left.createNodeList());
        union.addAll(right.createNodeList());

        MDPSolution combined = memory.forInstance(left.getInstance()).createSolutionRefreshMemory(union);
        combined.notifyUpdate();
        return List.of(combined);
    }
}
