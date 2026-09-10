package es.urjc.etsii.grafo.mmdp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.mmdp.model.MMDPInstance;
import es.urjc.etsii.grafo.mmdp.model.MMDPSolution;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

/**
 * Per-(instance, algorithm) time budget, mirroring the 10 s used by jmh. Mork autodetects this
 * class because it extends {@link TimeLimitCalculator}. Tunable via {@code -Dmmdp.timeLimitMillis}.
 */
public class MMDPTimeLimit extends TimeLimitCalculator<MMDPSolution, MMDPInstance> {

    private static final long BUDGET_MILLIS = Long.getLong("mmdp.timeLimitMillis", 10_000L);

    @Override
    public long timeLimitInMillis(MMDPInstance instance, Algorithm<MMDPSolution, MMDPInstance> algorithm) {
        return BUDGET_MILLIS;
    }
}
