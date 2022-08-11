package es.urjc.etsii.grafo.TSP.experiments;

import es.urjc.etsii.grafo.TSP.algorithms.constructives.TSPRandomConstructive;
import es.urjc.etsii.grafo.TSP.algorithms.neighborhood.InsertNeighborhood;
import es.urjc.etsii.grafo.TSP.algorithms.neighborhood.SwapNeighborhood;
import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class LocalSearchExperiment extends AbstractExperiment<TSPSolution, TSPInstance> {

    @Override
    public List<Algorithm<TSPSolution, TSPInstance>> getAlgorithms() {

        var algorithms = new ArrayList<Algorithm<TSPSolution, TSPInstance>>();

        boolean maximize = Mork.isMaximizing();
        algorithms.add(new SimpleAlgorithm<>("Random", new TSPRandomConstructive()));
        algorithms.add(new SimpleAlgorithm<>("RandomFIInsert", new TSPRandomConstructive(),
                new LocalSearchFirstImprovement<>(maximize, new InsertNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomBIInsert", new TSPRandomConstructive(),
                new LocalSearchBestImprovement<>(maximize, new InsertNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomFISwap", new TSPRandomConstructive(),
                new LocalSearchFirstImprovement<>(maximize, new SwapNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomBISwap", new TSPRandomConstructive(),
                new LocalSearchBestImprovement<>(maximize, new SwapNeighborhood())));

        return algorithms;
    }
}
