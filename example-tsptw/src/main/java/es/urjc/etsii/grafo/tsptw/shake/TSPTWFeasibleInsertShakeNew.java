package es.urjc.etsii.grafo.tsptw.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** Full-position variant of TSPTWFeasibleInsertShake, accepting complete feasible relocations. */
public class TSPTWFeasibleInsertShakeNew extends TSPTWFeasibleInsertShake {
    private final int attemptFactor;

    @AutoconfigConstructor
    public TSPTWFeasibleInsertShakeNew(@IntegerParam(min = 2, max = 10) int attemptFactor) {
        if (attemptFactor < 2 || attemptFactor > 10) throw new IllegalArgumentException("Attempt factor must be in [2, 10]");
        this.attemptFactor = attemptFactor;
    }

    public TSPTWFeasibleInsertShakeNew() { this(5); }

    @Override
    public TSPTWSolution shake(TSPTWSolution solution, int k) {
        TSPTWNewMoveUtil.shakeRelocations(solution, k, attemptFactor);
        return solution;
    }

    @Override
    public String toString() { return "TSPTWFeasibleInsertShakeNew{attemptFactor=" + attemptFactor + "}"; }
}
