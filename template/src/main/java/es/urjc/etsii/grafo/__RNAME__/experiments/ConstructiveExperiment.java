package es.urjc.etsii.grafo.__RNAME__.experiments;

import es.urjc.etsii.grafo.__RNAME__.constructives.__RNAME__RandomConstructive;
import es.urjc.etsii.grafo.__RNAME__.constructives.grasp.__RNAME__ListManager;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Config;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.solver.Mork;

import java.util.ArrayList;
import java.util.List;

public class ConstructiveExperiment extends AbstractExperiment<__RNAME__Solution, __RNAME__Instance> {


    private final __RNAME__Config config;

    public ConstructiveExperiment(__RNAME__Config config) {
        // Any config class can be requested via the constructor
        this.config = config;
    }

    @Override
    public List<Algorithm<__RNAME__Solution, __RNAME__Instance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations
        // TODO: Using this experiment as an example, after first test define your own experiments.
        boolean maximizing = Mork.isMaximizing();
        var algorithms = new ArrayList<Algorithm<__RNAME__Solution, __RNAME__Instance>>();
        var graspListManager = new __RNAME__ListManager();
        double[] alphaValues = {0d, 0.25d, 0.5d, 0.75d, 1d};

        // Add random constructive to list of algorithms to test
        // SimpleAlgorithm executes the given constructive and the (optional) local search methods once.
        algorithms.add(new SimpleAlgorithm<>(new __RNAME__RandomConstructive()));

        // Add GRASP constructive methods to experiment
        // if the alpha parameter is not given --> random alpha in range [0,1] for each construction
        var graspBuilder = new GraspBuilder<__RNAME__ListManager.__RNAME__GRASPMove, __RNAME__Solution, __RNAME__Instance>()
                //.withGreedyFunction()     // Optional, uncomment if a custom greedy function is used instead of the default
                .withMaximizing(maximizing)   // Change maximizing to either true or false if a greedy function is specified
                .withListManager(graspListManager);

        // Create variants using greedy random strategy
        graspBuilder.withStrategyGreedyRandom();
        algorithms.add(new SimpleAlgorithm<>(graspBuilder.withAlphaRandom().build()));
        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>(graspBuilder.withAlphaValue(alpha).build()));
        }

        // Create variants using random greedy strategy
        graspBuilder.withStrategyRandomGreedy();
        algorithms.add(new SimpleAlgorithm<>(graspBuilder.withAlphaRandom().build()));
        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>(graspBuilder.withAlphaValue(alpha).build()));
        }

        return algorithms;
    }
}
