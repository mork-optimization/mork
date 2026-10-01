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
        return List.of(new LMLS(config.getVariant(), LMLSParameters.PAPER, config.getMaxRestarts()));
    }
}
