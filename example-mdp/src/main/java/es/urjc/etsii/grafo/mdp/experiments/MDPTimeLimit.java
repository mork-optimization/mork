package es.urjc.etsii.grafo.mdp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

/**
 * Per-(instance, algorithm) time budget, mirroring the 2 s used by jmh. Mork autodetects this
 * class because it extends {@link TimeLimitCalculator}. Tunable via {@code -Dmdp.timeLimitMillis}.
 */
public class MDPTimeLimit extends TimeLimitCalculator<MDPSolution, MDPInstance> {

    private static final long BUDGET_MILLIS = Long.getLong("mdp.timeLimitMillis", 2_000L);

    @Override
    public long timeLimitInMillis(MDPInstance instance, Algorithm<MDPSolution, MDPInstance> algorithm) {
        return BUDGET_MILLIS;
    }
}
