package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.mreflp.MREFLPConfig;
import es.urjc.etsii.grafo.mreflp.model.*;

public class MREFLPTimeLimit extends TimeLimitCalculator<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConfig config;
    private final ReferenceCatalog references;

    public MREFLPTimeLimit(MREFLPConfig config, ReferenceCatalog references) {
        this.config = config;
        this.references = references;
    }

    @Override public long timeLimitInMillis(MREFLPInstance instance, Algorithm<MREFLPSolution, MREFLPInstance> algorithm) {
        Double seconds = config.getTimeLimitSeconds();
        if (seconds == null) {
            var reference = references.get(instance.getId(), "LMLS");
            // N-15 has no published timings; keep these cases usable until screening.
            seconds = reference == null || reference.timeToBestSeconds() == null ? 1.0 : reference.timeToBestSeconds();
        }
        // Published times are rounded to hundredths of a second and can be zero.
        return Math.max(1, Math.round(seconds * 1000));
    }
}
