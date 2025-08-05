package es.urjc.etsii.grafo.MST.experiments;

import es.urjc.etsii.grafo.MST.algorithms.KruskalAlgorithm;
import es.urjc.etsii.grafo.MST.algorithms.PrimAlgorithm;
import es.urjc.etsii.grafo.MST.model.MSTInstance;
import es.urjc.etsii.grafo.MST.model.MSTSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class PerformanceExperiment extends AbstractExperiment<MSTSolution, MSTInstance> {

    @Override
    public List<Algorithm<MSTSolution, MSTInstance>> getAlgorithms() {
        return List.of(new KruskalAlgorithm(), new PrimAlgorithm());
    }
}
