package es.urjc.etsii.grafo.__RNAME__.experiments;

import es.urjc.etsii.grafo.__RNAME__.constructives.__RNAME__RandomConstructive;
import es.urjc.etsii.grafo.__RNAME__.constructives.grasp.__RNAME__ListManager;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.solver.create.grasp.RandomGreedyGRASPConstructive;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public class ConstructiveExperiment extends AbstractExperiment<__RNAME__Solution, __RNAME__Instance> {

    public ConstructiveExperiment(@Value("${solver.maximizing}") boolean maximizing) {
        super(maximizing);
    }

    @Override
    public List<Algorithm<__RNAME__Solution, __RNAME__Instance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations
        boolean maximizing = super.isMaximizing();
        var algorithms = new ArrayList<Algorithm<__RNAME__Solution, __RNAME__Instance>>();
        var graspListManager = new __RNAME__ListManager();
        double[] alphaValues = {0d, 0.25d, 0.5d, 0.75d, 1d};

        algorithms.add(new SimpleAlgorithm<>(new __RNAME__RandomConstructive()));

        // no alpha parameter --> random alpha in range [0,1] for each construction
        algorithms.add(new SimpleAlgorithm<>(new GreedyRandomGRASPConstructive<>(graspListManager, maximizing)));
        algorithms.add(new SimpleAlgorithm<>(new RandomGreedyGRASPConstructive<>(graspListManager, maximizing)));

        for (double alpha : alphaValues) {
            algorithms.add(new SimpleAlgorithm<>(new GreedyRandomGRASPConstructive<>(graspListManager, alpha, maximizing)));
            algorithms.add(new SimpleAlgorithm<>(new RandomGreedyGRASPConstructive<>(graspListManager, alpha, maximizing)));
        }

        return algorithms;
    }
}
