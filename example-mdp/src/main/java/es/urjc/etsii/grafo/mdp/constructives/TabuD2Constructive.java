package es.urjc.etsii.grafo.mdp.constructives;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.scattersearch.TabuD2Memory;

/**
 * Memory-guided "good value" constructive of the WITH_MEMORY Scatter Search variant. It builds a
 * solution with the shared Tabu/D2 long-term memory (greedy contribution-plus-ponderation removal
 * down to {@code m}) and records the result in the memory, so successive constructions are biased
 * toward under-explored, high-quality nodes.
 *
 * <p>The passed (full) solution is ignored: the memory engine produces its own solution. The memory
 * is shared with the combinator through {@link TabuD2Memory}.
 */
public class TabuD2Constructive extends Constructive<MDPSolution, MDPInstance> {

    private final TabuD2Memory memory;

    public TabuD2Constructive(TabuD2Memory memory) {
        this.memory = memory;
    }

    @Override
    public MDPSolution construct(MDPSolution solution) {
        MDPSolution built = memory.forInstance(solution.getInstance()).createSolutionRefreshMemory();
        built.notifyUpdate();
        return built;
    }
}
