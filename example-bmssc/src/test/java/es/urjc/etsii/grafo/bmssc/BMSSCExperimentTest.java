package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.factories.GRGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.factories.RGGraspConstructiveFactory;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructor;
import es.urjc.etsii.grafo.bmssc.create.RandomConstructor;
import es.urjc.etsii.grafo.bmssc.experiment.BlacklistedComponents;
import es.urjc.etsii.grafo.bmssc.experiment.ClusteringTimeLimit;
import es.urjc.etsii.grafo.bmssc.experiment.FinalExperiment;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BMSSCExperimentTest {
    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void defaultRecipeAndRuntimeSettingsPreserveTheHistoricalComparison() throws Exception {
        var algorithms = new FinalExperiment().getAlgorithms();
        assertEquals(1, algorithms.size());
        var algorithm = assertInstanceOf(VNS.class, algorithms.getFirst());
        var constructive = assertInstanceOf(BMSSCGRASPConstructor.class, ReflectionTestUtils.getField(algorithm, "constructive"));
        assertEquals(0.68, ReflectionTestUtils.getField(constructive, "alpha"));
        assertInstanceOf(LocalSearchFirstImprovement.class, ReflectionTestUtils.getField(algorithm, "improver"));
        var shake = assertInstanceOf(StrategicOscillation.class, ReflectionTestUtils.getField(algorithm, "shake"));
        assertEquals(0.75, ReflectionTestUtils.getField(shake, "increment"));
        assertEquals(2, ReflectionTestUtils.getField(ReflectionTestUtils.getField(algorithm, "neighChange"), "maxK"));
        assertEquals(1_000_000, new ClusteringTimeLimit().timeLimitInMillis(instance(1, 1), algorithms.getFirst()));
        assertEquals("Cost", Main.OBJ.getName());
        try (var input = getClass().getResourceAsStream("/application.yml")) {
            Map<String, Object> config = new Yaml().load(input);
            Map<?, ?> solver = (Map<?, ?>) config.get("solver");
            assertEquals(1234, solver.get("seed"));
            assertEquals(30, solver.get("repetitions"));
            assertEquals(true, solver.get("parallelExecutor"));
            assertEquals(10, solver.get("nWorkers"));
            assertFalse(config.containsKey("custom"));
            assertFalse(config.containsKey("irace"));
            Map<?, ?> paths = (Map<?, ?>) ((Map<?, ?>) config.get("instances")).get("path");
            assertEquals("instances/", paths.get("default"));
            assertEquals("instances/instances.zip", paths.get("irace"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void discoversSafeComponentsAndReplaysCurrentJsonRecipes() {
        var inventory = new AlgorithmInventoryService(new BlacklistedComponents(),
                List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.bmssc,es.urjc.etsii.grafo.algorithms,"
                + "es.urjc.etsii.grafo.create,es.urjc.etsii.grafo.improve,es.urjc.etsii.grafo.shake,"
                + "es.urjc.etsii.grafo.solution.neighborhood");
        var components = inventory.getInventory().allComponents();
        assertTrue(components.containsAll(List.of(SimpleAlgorithm.class, BMSSCGRASPConstructor.class,
                RandomConstructor.class, SwapNeighborhood.class, StrategicOscillation.class)));
        assertFalse(components.contains(ReassignNeighborhood.class));
        assertFalse(inventory.getInventory().factories().containsKey("GreedyRandomGRASPConstructive"));
        var generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        var tuningSpace = String.join("\n", generator.toIraceParams(generator.buildTree(1000, 0)));
        assertTrue(tuningSpace.contains("BMSSCGRASPConstructor"));
        assertTrue(tuningSpace.contains("LocalSearchCachedBestImprovement"));
        assertFalse(tuningSpace.contains("ReassignNeighborhood"));
        assertFalse(tuningSpace.contains("GreedyRandomGRASPConstructive"));
        var builder = new AlgorithmBuilderService(inventory);
        for (String localSearch : new String[]{"LocalSearchFirstImprovement", "LocalSearchBestImprovement",
                "LocalSearchCachedBestImprovement"}) {
            var neighborhood = new ComponentSpec("SwapNeighborhood");
            Map<String, Object> parameters = localSearch.contains("Cached")
                    ? Map.of("neighborhood", neighborhood, "cacheSize", 4) : Map.of("neighborhood", neighborhood);
            var recipe = new ComponentSpec("VNS", Map.of("algorithmName", "Replay", "maxK", 2,
                    "constructive", new ComponentSpec("BMSSCGRASPConstructor", Map.of("alpha", 0.68)),
                    "shake", new ComponentSpec("StrategicOscillation", Map.of("increment", 0.75)),
                    "improver", new ComponentSpec(localSearch, parameters)));
            var direct = (Algorithm<BMSSCSolution, BMSSCInstance>) builder.buildAlgorithm(recipe);
            var replay = (Algorithm<BMSSCSolution, BMSSCInstance>) builder.buildAlgorithmFromJson(builder.toJson(recipe));
            direct.setBuilder(solutionBuilder());
            replay.setBuilder(solutionBuilder());
            initialize(44);
            var expected = direct.algorithm(instance(10, 3));
            initialize(44);
            var actual = replay.algorithm(instance(10, 3));
            assertFeasible(actual);
            assertEquals(expected.getCost(), actual.getCost());
            assertArrayEquals(assignment(expected), assignment(actual));
        }
    }

    @Test
    void boundedDefaultRunsReproduceSequentiallyAndConcurrently() throws Exception {
        var algorithm = new FinalExperiment().getAlgorithms().getFirst();
        algorithm.setBuilder(solutionBuilder());
        var instance = instance(10, 3);
        List<BMSSCSolution> expected = new ArrayList<>();
        for (int run = 0; run < 12; run++) expected.add(run(algorithm, instance, run));
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Future<BMSSCSolution>> pending = new ArrayList<>();
            for (int run = 0; run < 12; run++) {
                int seed = run;
                pending.add(executor.submit(() -> run(algorithm, instance, seed)));
            }
            for (int run = 0; run < pending.size(); run++) {
                var actual = pending.get(run).get(10, TimeUnit.SECONDS);
                assertFeasible(actual);
                assertEquals(expected.get(run).getCost(), actual.getCost());
                assertArrayEquals(assignment(expected.get(run)), assignment(actual));
            }
        }
    }

    private BMSSCSolution run(Algorithm<BMSSCSolution, BMSSCInstance> algorithm, BMSSCInstance instance, int seed) {
        initialize(seed);
        try {
            return algorithm.algorithm(instance);
        } finally {
            TimeControl.remove();
            Context.reset();
        }
    }

    private SolutionBuilder<BMSSCSolution, BMSSCInstance> solutionBuilder() {
        return new SolutionBuilder<>() {
            @Override
            public BMSSCSolution initializeSolution(BMSSCInstance instance) { return new BMSSCSolution(instance); }
        };
    }
}
