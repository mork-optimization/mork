package es.urjc.etsii.grafo.VRPOD.experiments;

import es.urjc.etsii.grafo.VRPOD.Main;
import es.urjc.etsii.grafo.VRPOD.algorithm.ILSConfig;
import es.urjc.etsii.grafo.VRPOD.algorithm.SeqExchangerILS;
import es.urjc.etsii.grafo.VRPOD.constructives.VRPODGRASPConstructive;
import es.urjc.etsii.grafo.VRPOD.destructives.RandomMovement;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODExtendedNeighborhood;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.shake.Shake;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FinalSotaExperiment extends AbstractExperiment<VRPODSolution, VRPODInstance> {

    @Override
    public List<Algorithm<VRPODSolution, VRPODInstance>> getAlgorithms() {
        return List.of(sotaAlgorithm());
    }

    public static Algorithm<VRPODSolution, VRPODInstance> sotaAlgorithm() {
        Supplier<Constructive<VRPODSolution, VRPODInstance>> grasp_0 = () -> new VRPODGRASPConstructive(0);
        Supplier<Constructive<VRPODSolution, VRPODInstance>> grasp_random = VRPODGRASPConstructive::new;
        Supplier<Shake<VRPODSolution, VRPODInstance>> shakeS = () -> new RandomMovement(1);
        Supplier<Improver<VRPODSolution, VRPODInstance>> improverS = () -> new LocalSearchBestImprovement<>(Main.OBJ, new VRPODExtendedNeighborhood());

        var sota =  new SeqExchangerILS("sota", 100, 2,
                new ILSConfig(25, grasp_0, shakeS, improverS),
                new ILSConfig(50, grasp_0, shakeS, improverS),
                new ILSConfig(25, grasp_random, shakeS, improverS),
                new ILSConfig(50, grasp_random, shakeS, improverS)
        );
        // Wrap algorithms as multistart with "infinite" iterations
        // Algorithms will automatically stop when they reach the timelimit for a given instance
        var multistart = new MultiStartAlgorithm<>("sota", Main.OBJ, sota, 1_000_000, 1_000_000, 1_000_000);
        return multistart;
    }
}
