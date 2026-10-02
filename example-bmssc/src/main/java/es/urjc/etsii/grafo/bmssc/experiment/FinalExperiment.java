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
        var algorithms = List.of(
                // autoconfig largo
                this.algorithmBuilder.buildFromJson("AutoL1", """
                        {
                                        "$component": "VNS",
                                        "maxK": 34,
                                        "constructive": {
                                            "$component": "BMSSCGRASPConstructor",
                                            "alpha": 0.85
                                        },
                                        "shake": {
                                            "$component": "DestroyRebuild",
                                            "constructive": {
                                                "$component": "GreedyRandomGRASPConstructive",
                                                "alpha": 0.5,
                                                "candidateListManager": {
                                                    "$component": "BMSSCListManagerNew",
                                                    "seedStrategy": "FARTHEST_FIRST"
                                                }
                                            },
                                            "destructive": {
                                                "$component": "RandomRemoval",
                                                "fraction": 0.16
                                            }
                                        },
                                        "improver": {
                                            "$component": "LocalSearchFirstImprovement",
                                            "neighborhood": {
                                                "$component": "ShuffledSwapNeighborhood"
                                            }
                                        }
                                    }
                        """),
                this.algorithmBuilder.buildFromJson("AutoL2", """
                
                        {
                                        "$component": "VNS",
                                        "maxK": 84,
                                        "constructive": {
                                            "$component": "BMSSCGRASPConstructor",
                                            "alpha": 0.6
                                        },
                                        "shake": {
                                            "$component": "DestroyRebuild",
                                            "constructive": {
                                                "$component": "GreedyRandomGRASPConstructive",
                                                "alpha": 0.33,
                                                "candidateListManager": {
                                                    "$component": "BMSSCListManagerNew",
                                                    "seedStrategy": "FARTHEST_FIRST"
                                                }
                                            },
                                            "destructive": {
                                                "$component": "RandomRemoval",
                                                "fraction": 0.09
                                            }
                                        },
                                        "improver": {
                                            "$component": "LocalSearchFirstImprovement",
                                            "neighborhood": {
                                                "$component": "ShuffledSwapNeighborhood"
                                            }
                                        }
                                    }
                """),

                this.algorithmBuilder.buildFromJson("AutoL3", """
                        {
                                        "$component": "VNS",
                                        "maxK": 74,
                                        "constructive": {
                                            "$component": "BMSSCGRASPConstructor",
                                            "alpha": 0.73
                                        },
                                        "shake": {
                                            "$component": "DestroyRebuild",
                                            "constructive": {
                                                "$component": "GreedyRandomGRASPConstructive",
                                                "alpha": 0.14,
                                                "candidateListManager": {
                                                    "$component": "BMSSCListManagerNew",
                                                    "seedStrategy": "FARTHEST_FIRST"
                                                }
                                            },
                                            "destructive": {
                                                "$component": "RandomRemoval",
                                                "fraction": 0.1
                                            }
                                        },
                                        "improver": {
                                            "$component": "LocalSearchFirstImprovement",
                                            "neighborhood": {
                                                "$component": "ShuffledSwapNeighborhood"
                                            }
                                        }
                                    }"""),
                // autoconfig corto
                algorithmBuilder.buildFromStringParams("AutoS1", """
                        ROOT=VNS ROOT_VNS.constructive=RandomConstructor ROOT_VNS.improver=LocalSearchFirstImprovement ROOT_VNS.improver_LocalSearchFirstImprovement.neighborhood=ShuffledSwapNeighborhood ROOT_VNS.maxK=41 ROOT_VNS.shake=DestroyRebuild ROOT_VNS.shake_DestroyRebuild.constructive=RegretConstructor ROOT_VNS.shake_DestroyRebuild.constructive_RegretConstructor.alpha=0.83 ROOT_VNS.shake_DestroyRebuild.destructive=WorstRemoval ROOT_VNS.shake_DestroyRebuild.destructive_WorstRemoval.fraction=0.2
                        """),
                algorithmBuilder.buildFromStringParams("AutoS2", """
                        ROOT=VNS ROOT_VNS.constructive=RandomConstructor ROOT_VNS.improver=LocalSearchFirstImprovement ROOT_VNS.improver_LocalSearchFirstImprovement.neighborhood=ShuffledSwapNeighborhood ROOT_VNS.maxK=43 ROOT_VNS.shake=DestroyRebuild ROOT_VNS.shake_DestroyRebuild.constructive=RegretConstructor ROOT_VNS.shake_DestroyRebuild.constructive_RegretConstructor.alpha=0.89 ROOT_VNS.shake_DestroyRebuild.destructive=WorstRemoval ROOT_VNS.shake_DestroyRebuild.destructive_WorstRemoval.fraction=0.09
                        """),
                algorithmBuilder.buildFromStringParams("AutoS3", """
                        ROOT=VNS ROOT_VNS.constructive=RandomConstructor ROOT_VNS.improver=LocalSearchFirstImprovement ROOT_VNS.improver_LocalSearchFirstImprovement.neighborhood=ShuffledSwapNeighborhood ROOT_VNS.maxK=14 ROOT_VNS.shake=DestroyRebuild ROOT_VNS.shake_DestroyRebuild.constructive=RegretConstructor ROOT_VNS.shake_DestroyRebuild.constructive_RegretConstructor.alpha=0.71 ROOT_VNS.shake_DestroyRebuild.destructive=WorstRemoval ROOT_VNS.shake_DestroyRebuild.destructive_WorstRemoval.fraction=0.24
                        """),

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
        );
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
