package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

class AutomaticIraceAlgorithmGeneratorTest {
    private static AutomaticAlgorithmBuilder<?, ?> generator;

    @BeforeAll
    static void setUp(){
        var config = new SolverConfig();
        config.setTreeDepth(4);
        var inventory = new AlgorithmInventoryService(new DefaultInventoryFilter(), List.of(), List.of());
        inventory.runComponentDiscovery("es.urjc.etsii.grafo");
        var candidateGenerator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var builderService = new AlgorithmBuilderService(inventory);
        generator = new AutomaticAlgorithmBuilder<>(config, candidateGenerator, builderService);
    }

//    @Test // TODO complete test
//    void failed1(){
//        String description = "ROOT_IteratedGreedy.constructive=CAPConstructive ROOT_IteratedGreedy.constructive_CAPConstructive.alpha=0.91 ROOT_IteratedGreedy.constructive_CAPConstructive.type='\"greedyB2\"' ROOT_IteratedGreedy.destructionReconstruction=CAPShake ROOT_IteratedGreedy.destructionReconstruction_CAPShake.type='\"7\"' ROOT_IteratedGreedy.improver=VND ROOT_IteratedGreedy.improver_VND.improver1=NullImprover ROOT_IteratedGreedy.improver_VND.improver2=CAPLS ROOT_IteratedGreedy.improver_VND.improver2_CAPLS.type='\"exc_fi\"' ROOT_IteratedGreedy.improver_VND.improver3=CAPLS ROOT_IteratedGreedy.improver_VND.improver3_CAPLS.type='\"exc_fi\"' ROOT_IteratedGreedy.maxIterations=480106062 ROOT_IteratedGreedy.stopIfNotImprovedIn=586731171";
//        var algorithmConfig = new AlgorithmConfiguration(description.split("\s+"));
//        var result = generator.buildAlgorithmString(algorithmConfig);
//        System.out.println(result);
//    }
}