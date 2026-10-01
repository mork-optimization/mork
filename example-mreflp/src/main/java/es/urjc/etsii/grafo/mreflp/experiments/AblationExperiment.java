package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.mreflp.MREFLPConfig;
import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import java.util.ArrayList;
import java.util.List;

public class AblationExperiment extends AbstractExperiment<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConfig config;
    public AblationExperiment(MREFLPConfig config) { this.config = config; }
    @Override public List<Algorithm<MREFLPSolution, MREFLPInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<MREFLPSolution, MREFLPInstance>>();
        for (var variant : LMLSVariant.values()) algorithms.add(new LMLS(variant, LMLSParameters.PAPER, config.getMaxRestarts()));
        return algorithms;
    }
}
