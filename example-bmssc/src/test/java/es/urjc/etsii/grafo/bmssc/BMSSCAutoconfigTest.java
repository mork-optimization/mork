package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.bmssc.experiment.BlacklistedComponents;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BMSSCAutoconfigTest {
    private AlgorithmInventoryService inventory;

    @BeforeEach
    void setup() {
        initialize(1234);
        inventory = new AlgorithmInventoryService(new BlacklistedComponents(), List.of(),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.algorithms,es.urjc.etsii.grafo.create,es.urjc.etsii.grafo.improve,"
                + "es.urjc.etsii.grafo.shake,es.urjc.etsii.grafo.solution.neighborhood,es.urjc.etsii.grafo.bmssc");
    }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void discoveryRetainsOriginalRootAlgorithmsAndBlacklist() {
        var generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var names = new HashSet<String>();
        for (var root : generator.buildTree(1000, 0)) names.add(root.className());
        assertEquals(Set.of("SimpleAlgorithm", "VNS", "IteratedGreedy", "MultistartOnlyBestAppliesLS"), names);
        var components = inventory.getInventory().componentByName().keySet();
        assertTrue(components.containsAll(Set.of("BMSSCGRASPConstructor", "RandomConstructor", "SwapNeighborhood",
                "FirstImpLS", "BestImpLS", "ShakeImprover", "StrategicOscillation")));
        assertFalse(components.contains("ReassignNeighborhood"));
        assertFalse(components.contains("GreedyRandomGRASPConstructive"));
        assertFalse(components.contains("RandomGreedyGRASPConstructive"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void bothOriginalStartingConfigurationsBuildAndExecute() throws Exception {
        var codec = new ComponentSpecJsonCodec();
        var seeds = codec.parseList(Files.readString(Path.of("src/main/resources/irace/initial-configurations.json")));
        assertEquals(2, seeds.size());
        assertEquals(List.of("MultistartOnlyBestAppliesLS", "VNS"), List.of(seeds.get(0).component(), seeds.get(1).component()));
        var builder = new AlgorithmBuilderService(inventory);
        for (var seed : seeds) {
            var algorithm = (Algorithm<BMSSCSolution, BMSSCInstance>) builder.buildAlgorithmFromJson(codec.toJson(seed));
            algorithm.setBuilder(new SolutionBuilder<>() {
                @Override public BMSSCSolution initializeSolution(BMSSCInstance instance) { return new BMSSCSolution(instance); }
            });
            TimeControl.setMaxExecutionTime(20, TimeUnit.MILLISECONDS);
            TimeControl.start();
            assertFeasible(algorithm.algorithm(instance(9, 3)));
            TimeControl.remove();
        }
    }
}
