package es.urjc.etsii.grafo.exgraph.experiments;

import es.urjc.etsii.grafo.exgraph.algorithms.KruskalAlg;
import es.urjc.etsii.grafo.exgraph.algorithms.PrimAlg;
import es.urjc.etsii.grafo.exgraph.model.MSTInstance;
import es.urjc.etsii.grafo.exgraph.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class MinSpanTreeExp extends AbstractExperiment<MSTSolution, MSTInstance> {

    @Override
    public List<Algorithm<MSTSolution, MSTInstance>> getAlgorithms() {
        return List.of(
                new KruskalAlg(),
                new PrimAlg()
        );
    }
}
