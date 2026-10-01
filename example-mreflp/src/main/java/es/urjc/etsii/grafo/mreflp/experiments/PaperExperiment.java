package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.mreflp.MREFLPConfig;
import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import java.util.List;

public class PaperExperiment extends AbstractExperiment<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConfig config;
    public PaperExperiment(MREFLPConfig config) { this.config = config; }
    @Override public List<Algorithm<MREFLPSolution, MREFLPInstance>> getAlgorithms() {
        return List.of(paperAlgorithm(config.getVariant(), config.getMaxRestarts()));
    }

    /** Table 1: epsilon, stagnation iterations, tenure, reward, penalty, compensation, smoothing. */
    public static LMLS paperAlgorithm(LMLSVariant variant, int maxRestarts) {
        return new LMLS(variant, 0.6, 20, 3, 0.1, 0.2, 0.3, 0.3, maxRestarts);
    }
}
