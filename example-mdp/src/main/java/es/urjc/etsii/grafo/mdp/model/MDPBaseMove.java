package es.urjc.etsii.grafo.mdp.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base move for MDP. Exposes the objective delta ({@link #getScoreChange()}) that the
 * {@code Objective} uses for incremental evaluation, exactly like the base moves of the other
 * {@code mork-full} problems.
 */
public abstract class MDPBaseMove extends Move<MDPSolution, MDPInstance> {

    protected double scoreChange;

    public MDPBaseMove(MDPSolution solution) {
        super(solution);
    }

    public double getScoreChange() {
        return scoreChange;
    }
}
