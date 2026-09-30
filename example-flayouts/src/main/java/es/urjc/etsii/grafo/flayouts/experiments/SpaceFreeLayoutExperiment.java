package es.urjc.etsii.grafo.flayouts.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.flayouts.constructives.FLPRandomConstructiveNew;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPRelocateNeighNew;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;

import java.util.List;

/** Runnable example combining random construction with relocation local search. */
public class SpaceFreeLayoutExperiment extends AbstractExperiment<FLPSolution, FLPInstance> {
    @Override
    public List<Algorithm<FLPSolution, FLPInstance>> getAlgorithms() {
        return List.of(new SimpleAlgorithm<>("Random-Relocate",
                new FLPRandomConstructiveNew(),
                new LocalSearchBestImprovement<>(new FLPRelocateNeighNew())));
    }
}
