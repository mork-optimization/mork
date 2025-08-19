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

public class FinalExperiment extends AbstractExperiment<VRPODSolution, VRPODInstance> {

    private final AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder;

    public FinalExperiment(AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder) {
        this.builder = builder;
    }

    @Override
    public List<Algorithm<VRPODSolution, VRPODInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<VRPODSolution, VRPODInstance>>();

//        String[] iraceOutput = """
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.47 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=36 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=771701 ROOT_IteratedGreedy.stopIfNotImprovedIn=683424
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.38 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=35 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=796449 ROOT_IteratedGreedy.stopIfNotImprovedIn=727694
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.23 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=29 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=InsertNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=232978 ROOT_IteratedGreedy.stopIfNotImprovedIn=396721
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.46 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=39 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=891237 ROOT_IteratedGreedy.stopIfNotImprovedIn=685401
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.32 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=22 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=InsertNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=438039 ROOT_IteratedGreedy.stopIfNotImprovedIn=358946
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.28 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=20 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=InsertNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=552761 ROOT_IteratedGreedy.stopIfNotImprovedIn=700353
//                ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.9 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=10 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=InsertNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=467074 ROOT_IteratedGreedy.stopIfNotImprovedIn=400000
//                """.split("\n");

        //algorithms.add(sotaAlgorithm());
//        for (int i = 0; i < iraceOutput.length; i++) {
//            if (!iraceOutput[i].isBlank()) {
//                var algorithm = builder.buildFromStringParams(iraceOutput[i].trim());
//                // Wrap algorithms as multistart with "infinite" iterations
//                // Algorithms will automatically stop when they reach the timelimit for a given instance
//                var multistart = new MultiStartAlgorithm<>("ac"+i, algorithm, 1_000_000, 1_000_000, 1_000_000);
//                algorithms.add(multistart);
//            }
//        }

        var algorithm = builder.buildFromStringParams("ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.47 ROOT_IteratedGreedy.destructionReconstruction=RandomMovement ROOT_IteratedGreedy.destructionReconstruction_RandomMovement.multiplier=36 ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver1_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver2=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver2_LocalSearchBestImprovement.neighborhood=RouteToODNeigh ROOT_IteratedGreedy.improver_VND.improver3=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_VND.improver3_LocalSearchBestImprovement.neighborhood=VRPODExtendedNeighborhood ROOT_IteratedGreedy.maxIterations=771701 ROOT_IteratedGreedy.stopIfNotImprovedIn=683424");
        var multistart = new MultiStartAlgorithm<>("AutoConfig", Main.OBJ, algorithm, 1_000_000, 1_000_000, 1_000_000);
        algorithms.add(multistart);
        algorithms.add(FinalSotaExperiment.sotaAlgorithm());
        return algorithms;
    }
}
