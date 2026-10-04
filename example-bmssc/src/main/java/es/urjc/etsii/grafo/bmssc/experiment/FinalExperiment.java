package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
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

import java.util.ArrayList;
import java.util.List;

public class FinalExperiment extends AbstractExperiment<BMSSCSolution, BMSSCInstance> {

    private final AutomaticAlgorithmBuilder<BMSSCSolution, BMSSCInstance> algorithmBuilder;

    public FinalExperiment(AutomaticAlgorithmBuilder<BMSSCSolution, BMSSCInstance> algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
    }

    @Override
    public List<Algorithm<BMSSCSolution, BMSSCInstance>> getAlgorithms() {
        var algorithms = new ArrayList<>(algorithmBuilder.buildTopElites("autoconfig-final-elites.json", 3));

        algorithms.addAll(List.of(
                // Paper autoconfig y sota paper original
                new VNS<>("VNS-Autoconfig-Paper", 2,
                        new BMSSCGRASPConstructor(0.68),
                        new StrategicOscillation(0.75),
                        new LocalSearchFirstImprovement<>(new SwapNeighborhood())
                ),
                new MultistartOnlyBestAppliesLS("Sota-Reimplementation", 100,
                        new BMSSCGRASPConstructor(0.75),
                        new ShakeImprover(new FirstImpLS(), new StrategicOscillation(0.75))
                )
        ));
        return makeMultistart(algorithms);
    }

    public List<Algorithm<BMSSCSolution, BMSSCInstance>> makeMultistart(List<Algorithm<BMSSCSolution, BMSSCInstance>> algorithms){
        var multistarts = new ArrayList<Algorithm<BMSSCSolution, BMSSCInstance>>();
        for (var algorithm: algorithms){
            multistarts.add(makeMultistart(algorithm));
        }
        return multistarts;
    }

    public Algorithm<BMSSCSolution, BMSSCInstance> makeMultistart(Algorithm<BMSSCSolution, BMSSCInstance> algorithm){
        var name = algorithm.getName();
        return new MultiStartAlgorithm<>(name, Main.OBJ, algorithm, 1_000_000, 1_000_000, 1_000_000);
    }
}
