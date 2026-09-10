package es.urjc.etsii.grafo.cph.model;

import es.urjc.etsii.grafo.solution.Move;

/**
 * Base move for CPH. Exposes the objective delta ({@link #getScoreChange()}) that the
 * {@code Objective} uses for incremental evaluation.
 */
public abstract class CPHBaseMove extends Move<CPHSolution, CPHInstance> {

    protected double scoreChange;

    public CPHBaseMove(CPHSolution solution) {
        super(solution);
    }

    public double getScoreChange() {
        return scoreChange;
    }
}
