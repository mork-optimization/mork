package es.urjc.etsii.grafo.TSPTW.experiments;

import es.urjc.etsii.grafo.TSPTW.alg.GVNS;
import es.urjc.etsii.grafo.TSPTW.constructives.TSPTWRandomConstructive;
import es.urjc.etsii.grafo.TSPTW.constructives.grasp.TSPTWListManager;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWConfig;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWInstance;
import es.urjc.etsii.grafo.TSPTW.model.TSPTWSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.util.Context;

import java.util.ArrayList;
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
