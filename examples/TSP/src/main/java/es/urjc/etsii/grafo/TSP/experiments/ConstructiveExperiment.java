package es.urjc.etsii.grafo.TSP.experiments;

import es.urjc.etsii.grafo.TSP.constructives.TSPRandomConstructive;
import es.urjc.etsii.grafo.TSP.constructives.grasp.TSPListManager;
import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.solver.create.grasp.RandomGreedyGRASPConstructive;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class ConstructiveExperiment extends AbstractExperiment<TSPSolution, TSPInstance> {

    public ConstructiveExperiment(SolverConfig solverConfig) {
        super(solverConfig.isMaximizing());
    }

    @Override
    public List<Algorithm<TSPSolution, TSPInstance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations
        // TODO: Using this experiment as an example, after first test define your own experiments.
        boolean maximizing = super.isMaximizing();
        var algorithms = new ArrayList<Algorithm<TSPSolution, TSPInstance>>();
        var graspListManager = new TSPListManager();
        double[] alphaValues = {0d, 0.25d, 0.5d, 0.75d, 1d};

        // Add random constructive to list of algorithms to test
        // SimpleAlgorithm executes the given constructive and the (optional) local search methods once.
        algorithms.add(new SimpleAlgorithm<>(new TSPRandomConstructive()));

        // Add GRASP constructive methods to experiment
        // if the alpha parameter is not given --> random alpha in range [0,1] for each construction
        algorithms.add(new SimpleAlgorithm<>(new GreedyRandomGRASPConstructive<>(graspListManager, maximizing)));
        algorithms.add(new SimpleAlgorithm<>(new RandomGreedyGRASPConstructive<>(graspListManager, maximizing)));

        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>(new GreedyRandomGRASPConstructive<>(graspListManager, alpha, maximizing)));
            algorithms.add(new SimpleAlgorithm<>(new RandomGreedyGRASPConstructive<>(graspListManager, alpha, maximizing)));
        }

        return algorithms;
    }
}
