package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.model.TSPTWConfig;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class ValidationExperiment extends AbstractExperiment<TSPTWSolution, TSPTWInstance> {

    private final TSPTWConfig config;

    public ValidationExperiment(TSPTWConfig config) {
        // Any config class can be requested via the constructor
        this.config = config;
    }

    @Override
    public List<Algorithm<TSPTWSolution, TSPTWInstance>> getAlgorithms() {
        return List.of(new GVNS());
    }
}
