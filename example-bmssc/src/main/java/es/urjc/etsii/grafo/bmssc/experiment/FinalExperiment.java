package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.alg.MultistartOnlyBestAppliesLS;
import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructor;
import es.urjc.etsii.grafo.bmssc.improve.FirstImpLS;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.sol.SwapNeighborhood;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;

import java.util.List;

public class FinalExperiment extends AbstractExperiment<BMSSCSolution, BMSSCInstance> {
    @Override
    public List<Algorithm<BMSSCSolution, BMSSCInstance>> getAlgorithms() {
        return List.of(
                new VNS<>("VNS", 2,
                        new BMSSCGRASPConstructor(0.68),
                        new StrategicOscillation(0.75),
                        new LocalSearchFirstImprovement<>(new SwapNeighborhood())
                ),
                sotaAlgorithm()
        );
    }

    public Algorithm<BMSSCSolution, BMSSCInstance> sotaAlgorithm(){
        var algorithm = new MultistartOnlyBestAppliesLS("Reimplementation", 100, new BMSSCGRASPConstructor(0.75), new ShakeImprover(new FirstImpLS(), new StrategicOscillation(0.75)));
        var multistart = new MultiStartAlgorithm<>("sota", Main.OBJ, algorithm, 1_000_000, 1_000_000, 1_000_000);
        return multistart;
    }
}
