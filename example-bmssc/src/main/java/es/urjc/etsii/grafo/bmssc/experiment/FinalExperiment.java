package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.alg.MultistartOnlyBestAppliesLS;
import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructor;
import es.urjc.etsii.grafo.bmssc.improve.FirstImpLS;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class FinalExperiment extends AbstractExperiment<BMSSCSolution, BMSSCInstance> {

    private final AutomaticAlgorithmBuilder<BMSSCSolution, BMSSCInstance> builder;

    public FinalExperiment(AutomaticAlgorithmBuilder<BMSSCSolution, BMSSCInstance> builder) {
        this.builder = builder;
    }

    @Override
    public List<Algorithm<BMSSCSolution, BMSSCInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<BMSSCSolution, BMSSCInstance>>();

//        String[] iraceOutput = """
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.68 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.03 ROOT_VNS.maxK=3 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.72
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.72 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.06 ROOT_VNS.maxK=3 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.82
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.89 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.15 ROOT_VNS.maxK=6 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.58
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.62 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.01 ROOT_VNS.maxK=4 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.6
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.67 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.03 ROOT_VNS.maxK=4 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.78
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.68 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=FirstImpLS ROOT_VNS.maxK=2 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.75
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.67 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.73 ROOT_VNS.maxK=1 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.55
//                ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.79 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=ShakeImprover ROOT_VNS.improver_ShakeImprover.improver=FirstImpLS ROOT_VNS.improver_ShakeImprover.shake=StrategicOscillation ROOT_VNS.improver_ShakeImprover.shake_StrategicOscillation.increment=0.1 ROOT_VNS.maxK=2 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.6
//                """.split("\n");
//
//        for (int i = 0; i < iraceOutput.length; i++) {
//            if (!iraceOutput[i].isBlank()) {
//                var algorithm = builder.buildFromStringParams(iraceOutput[i].trim());
//                // Wrap algorithms as multistart with "infinite" iterations, so we are consistent with the autoconfig engine.
//                // Algorithms will automatically stop when they reach the timelimit for a given instance
//                var multistart = new MultiStartAlgorithm<>("ac" + i, algorithm, 1_000_000, 1_000_000, 1_000_000);
//                algorithms.add(multistart);
//            }
//        }
        var auto = builder.buildFromStringParams("ROOT=VNS ROOT_VNS.constructive=GreedyRandomGRASPConstructive ROOT_VNS.constructive_GreedyRandomGRASPConstructive.alpha=0.68 ROOT_VNS.constructive_GreedyRandomGRASPConstructive.candidateListManager=BMSSCListManager ROOT_VNS.improver=FirstImpLS ROOT_VNS.maxK=2 ROOT_VNS.shake=StrategicOscillation ROOT_VNS.shake_StrategicOscillation.increment=0.75");
        algorithms.add(auto);
        //algorithms.add(sotaAlgorithm());

        return algorithms;
    }

    public Algorithm<BMSSCSolution, BMSSCInstance> sotaAlgorithm(){
        var algorithm = new MultistartOnlyBestAppliesLS("Reimplementation", 100, new BMSSCGRASPConstructor(0.75), new ShakeImprover(new FirstImpLS(), new StrategicOscillation(0.75)));
        var multistart = new MultiStartAlgorithm<>("sota", Main.OBJ, algorithm, 1_000_000, 1_000_000, 1_000_000);
        return multistart;
    }

}
