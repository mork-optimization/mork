package es.urjc.etsii.grafo.graphs.experiments;

import es.urjc.etsii.grafo.graphs.algorithms.FloydWharshallAlg;
import es.urjc.etsii.grafo.graphs.algorithms.NDijkstraAlg;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class ShortestPathsExp extends AbstractExperiment<MSTSolution, MSTInstance> {

    @Override
    public List<Algorithm<MSTSolution, MSTInstance>> getAlgorithms() {
        return List.of(
                new FloydWharshallAlg(),
                new NDijkstraAlg()
        );
    }
}
