package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;
import es.urjc.etsii.grafo.mreflp.MREFLPConfig;
import es.urjc.etsii.grafo.mreflp.model.*;

public class MREFLPTimeLimit extends TimeLimitCalculator<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConfig config;
    public MREFLPTimeLimit(MREFLPConfig config) { this.config = config; }
    @Override public long timeLimitInMillis(MREFLPInstance instance, Algorithm<MREFLPSolution, MREFLPInstance> algorithm) {
        return Math.max(1, (long) (config.getTimeLimitSeconds() * 1000));
    }
}
