package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.model.TSPTWConfig;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class FinalExperiment extends AbstractExperiment<TSPTWSolution, TSPTWInstance> {

    private final TSPTWConfig config;
    private final AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> builder;

    public FinalExperiment(TSPTWConfig config, AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> builder) {
        // Any config class can be requested via the constructor
        this.config = config;
        this.builder = builder;
    }

    @Override
    public List<Algorithm<TSPTWSolution, TSPTWInstance>> getAlgorithms() {
        var algorithms = new ArrayList<>(builder.buildTopElites("autoconfig-final-elites.json", 3));

        algorithms.add(new GVNS());
        return makeMultistart(algorithms);
    }

    public List<Algorithm<TSPTWSolution, TSPTWInstance>> makeMultistart(List<Algorithm<TSPTWSolution, TSPTWInstance>> algorithms){
        var multistarts = new ArrayList<Algorithm<TSPTWSolution, TSPTWInstance>>();
        for (var algorithm: algorithms){
            multistarts.add(makeMultistart(algorithm));
        }
        return multistarts;
    }

    public Algorithm<TSPTWSolution, TSPTWInstance> makeMultistart(Algorithm<TSPTWSolution, TSPTWInstance> algorithm){
        var name = algorithm.getName();
        return new MultiStartAlgorithm<>(name, Main.OBJECTIVE, algorithm, 1_000_000, 1_000_000, 1_000_000);
    }
}
