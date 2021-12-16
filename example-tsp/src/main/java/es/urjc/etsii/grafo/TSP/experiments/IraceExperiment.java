package es.urjc.etsii.grafo.TSP.experiments;

import es.urjc.etsii.grafo.TSP.algorithms.constructives.TSPRandomConstructive;
import es.urjc.etsii.grafo.TSP.algorithms.neighborhood.InsertNeighborhood;
import es.urjc.etsii.grafo.TSP.algorithms.neighborhood.SwapNeighborhood;
import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearch;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.solver.irace.IraceAlgorithmGenerator;
import es.urjc.etsii.grafo.solver.irace.IraceRuntimeConfiguration;

public class IraceExperiment extends IraceAlgorithmGenerator<TSPSolution, TSPInstance> {

    @Override
    public Algorithm<TSPSolution, TSPInstance> buildAlgorithm(IraceRuntimeConfiguration config) {

        var localSearchName = config.getValue("localsearch").orElseThrow();
        var localSearchStrategy = config.getValue("localSearchStrategy").orElseThrow();
        var localSearch = buildLocalSearch(localSearchName, localSearchStrategy);
        var constructive = new TSPRandomConstructive();
        return new SimpleAlgorithm<>(constructive, localSearch);
    }

    private LocalSearch<? extends Move<TSPSolution, TSPInstance>, TSPSolution, TSPInstance> buildLocalSearch(String localSearchName, String localSearchStrategy) {

        var neighborhood = switch (localSearchName) {
            case "insert" -> new InsertNeighborhood();
            case "swap" -> new SwapNeighborhood();
            default -> throw new IllegalArgumentException("Not implemented: " + localSearchName);
        };

        return switch (localSearchStrategy) {
            case "first" -> new LocalSearchFirstImprovement<>(false, neighborhood);
            case "best" -> new LocalSearchBestImprovement<>(false, neighborhood);
            default -> throw new IllegalArgumentException("Not implemented: " + localSearchStrategy);
        };
    }
}