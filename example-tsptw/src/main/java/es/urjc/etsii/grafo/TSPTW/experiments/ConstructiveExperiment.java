package es.urjc.etsii.grafo.TSPTW.experiments;

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

public class ConstructiveExperiment extends AbstractExperiment<TSPTWSolution, TSPTWInstance> {


    private final TSPTWConfig config;

    public ConstructiveExperiment(TSPTWConfig config) {
        // Any config class can be requested via the constructor
        this.config = config;
    }

    @Override
    public List<Algorithm<TSPTWSolution, TSPTWInstance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations
        // TODO: Using this experiment as an example, after first test define your own experiments.
        var algorithms = new ArrayList<Algorithm<TSPTWSolution, TSPTWInstance>>();
        var graspListManager = new TSPTWListManager();
        double[] alphaValues = {0d, 0.25d, 0.5d, 0.75d, 1d};

        // Add random constructive to list of algorithms to test
        // SimpleAlgorithm executes the given constructive and the (optional) local search methods once.
        algorithms.add(new SimpleAlgorithm<>("Random", new TSPTWRandomConstructive()));

        // Add GRASP constructive methods to experiment
        // if the alpha parameter is not given --> random alpha in range [0,1] for each construction
        var graspBuilder = new GraspBuilder<TSPTWListManager.TSPTWGRASPMove, TSPTWSolution, TSPTWInstance>()
                //.withGreedyFunction()     // Optional, uncomment if a custom greedy function is used instead of the default (move get value)
                .withObjective(Context.getMainObjective())   // If using the default objective this line can be removed, but you can configure any objective or secondary function you want here, for example in case of a flat landscape you may want to use a custom greedy function
                .withListManager(graspListManager);

        // Create variants using greedy random strategy
        graspBuilder.withStrategyGreedyRandom();
        algorithms.add(new SimpleAlgorithm<>("GR-Random", graspBuilder.withAlphaRandom().build()));
        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>("GR-"+alpha, graspBuilder.withAlphaValue(alpha).build()));
        }

        // Create variants using random greedy strategy
        graspBuilder.withStrategyRandomGreedy();
        algorithms.add(new SimpleAlgorithm<>("RG-Random", graspBuilder.withAlphaRandom().build()));
        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>("RG-"+alpha, graspBuilder.withAlphaValue(alpha).build()));
        }

        return algorithms;
    }
}
