package es.urjc.etsii.grafo.cph.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.cph.model.CPHInstance;
import es.urjc.etsii.grafo.cph.model.CPHSolution;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

/**
 * Per-(instance, algorithm) time budget, mirroring the 10 s used by jmh. Mork autodetects this
 * class because it extends {@link TimeLimitCalculator}. Tunable via {@code -Dcph.timeLimitMillis}.
 */
public class CPHTimeLimit extends TimeLimitCalculator<CPHSolution, CPHInstance> {

    private static final long BUDGET_MILLIS = Long.getLong("cph.timeLimitMillis", 10_000L);

    @Override
    public long timeLimitInMillis(CPHInstance instance, Algorithm<CPHSolution, CPHInstance> algorithm) {
        return BUDGET_MILLIS;
    }
}
