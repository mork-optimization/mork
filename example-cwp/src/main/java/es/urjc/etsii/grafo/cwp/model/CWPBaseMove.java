package es.urjc.etsii.grafo.cwp.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base move for CWP. Exposes the objective delta ({@link #getScoreChange()}) that the
 * {@code Objective} uses for incremental evaluation.
 */
public abstract class CWPBaseMove extends Move<CWPSolution, CWPInstance> {

    protected double scoreChange;

    public CWPBaseMove(CWPSolution solution) {
        super(solution);
    }

    public double getScoreChange() {
        return scoreChange;
    }
}
