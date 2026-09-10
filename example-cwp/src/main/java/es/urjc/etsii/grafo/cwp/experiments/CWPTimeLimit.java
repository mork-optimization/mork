package es.urjc.etsii.grafo.cwp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.cwp.model.CWPInstance;
import es.urjc.etsii.grafo.cwp.model.CWPSolution;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

/**
 * Per-(instance, algorithm) time budget, mirroring the 10 s used by jmh. Mork autodetects this
 * class because it extends {@link TimeLimitCalculator}. Tunable via {@code -Dcwp.timeLimitMillis}.
 */
public class CWPTimeLimit extends TimeLimitCalculator<CWPSolution, CWPInstance> {

    private static final long BUDGET_MILLIS = Long.getLong("cwp.timeLimitMillis", 10_000L);

    @Override
    public long timeLimitInMillis(CWPInstance instance, Algorithm<CWPSolution, CWPInstance> algorithm) {
        return BUDGET_MILLIS;
    }
}
