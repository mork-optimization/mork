package es.urjc.etsii.grafo.tsp.experiments;

import es.urjc.etsii.grafo.tsp.algorithms.constructives.TSPRandomConstructive;
import es.urjc.etsii.grafo.tsp.model.neighs.InsertNeighborhood;
import es.urjc.etsii.grafo.tsp.model.neighs.SwapNeighborhood;
import es.urjc.etsii.grafo.tsp.model.TSPInstance;
import es.urjc.etsii.grafo.tsp.model.TSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;

import java.util.ArrayList;
import java.util.List;

public class LocalSearchExperiment extends AbstractExperiment<TSPSolution, TSPInstance> {

    @Override
    public List<Algorithm<TSPSolution, TSPInstance>> getAlgorithms() {

        var algorithms = new ArrayList<Algorithm<TSPSolution, TSPInstance>>();

        algorithms.add(new SimpleAlgorithm<>("Random", new TSPRandomConstructive()));
        algorithms.add(new SimpleAlgorithm<>("RandomFIInsert", new TSPRandomConstructive(),
                new LocalSearchFirstImprovement<>(new InsertNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomBIInsert", new TSPRandomConstructive(),
                new LocalSearchBestImprovement<>(new InsertNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomFISwap", new TSPRandomConstructive(),
                new LocalSearchFirstImprovement<>(new SwapNeighborhood())));
        algorithms.add(new SimpleAlgorithm<>("RandomBISwap", new TSPRandomConstructive(),
                new LocalSearchBestImprovement<>(new SwapNeighborhood())));

        return algorithms;
    }
}
