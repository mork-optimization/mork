package es.urjc.etsii.grafo.mmdp.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base move for MMDP. Exposes the objective delta ({@link #getScoreChange()}) that the
 * {@code Objective} uses for incremental evaluation.
 */
public abstract class MMDPBaseMove extends Move<MMDPSolution, MMDPInstance> {

    protected double scoreChange;

    public MMDPBaseMove(MMDPSolution solution) {
        super(solution);
    }

    public double getScoreChange() {
        return scoreChange;
    }
}
