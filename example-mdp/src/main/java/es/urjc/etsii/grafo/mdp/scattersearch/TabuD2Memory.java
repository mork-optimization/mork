package es.urjc.etsii.grafo.mdp.scattersearch;

import es.urjc.etsii.grafo.mdp.model.MDPInstance;

/**
 * Per-instance holder for the shared Tabu/D2 memory.
 *
 * <p>In the hand-written port the {@link TabuD2Calculator} was created once per (instance, run) and
 * handed to both the memory-guided constructive and combinator, so its long-term memory accumulated
 * across construction <i>and</i> combination. Mork, however, builds a single Scatter Search whose
 * constructive and combinator are reused across every instance — the memory cannot live in the
 * builder because it is instance-specific.
 *
 * <p>This holder bridges the gap: the constructive and combinator of one Scatter Search config share
 * one {@code TabuD2Memory}, and the first time a new instance is seen a fresh {@link TabuD2Calculator}
 * (empty memory) is created and reused for the rest of that instance's run. It relies on Mork solving
 * instances sequentially ({@code solver.parallelExecutor: false}, as configured for MDP).
 */
public class TabuD2Memory {

    private MDPInstance instance;
    private TabuD2Calculator calculator;

    /** Return the calculator for this instance, creating a fresh (empty-memory) one on instance change. */
    public TabuD2Calculator forInstance(MDPInstance instance) {
        if (this.instance != instance) {
            this.instance = instance;
            this.calculator = new TabuD2Calculator(instance);
        }
        return this.calculator;
    }
}
