package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.builder.*;
import es.urjc.etsii.grafo.autoconfig.factories.*;
import es.urjc.etsii.grafo.autoconfig.fill.*;
import es.urjc.etsii.grafo.autoconfig.generator.*;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.irace.params.*;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.mreflp.autoconfig.MREFLPInventoryFilterNew;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.mreflp.MREFLPTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MREFLPNewAutoconfigTest {
    private static AlgorithmInventoryService inventory;
    private static AlgorithmCandidateGenerator generator;
    private static AlgorithmBuilderService builder;
    private static AutomaticAlgorithmBuilder<MREFLPSolution, MREFLPInstance> automatic;
    private static AutoconfigSearchSpace space;
    private static List<ComponentSpec> compositions;

    @BeforeAll static void discover() throws IOException {
        context(1234);
        var productionFilter = new MREFLPInventoryFilterNew();
        // Keep the legacy sequential fixture covered independently of the production inventory.
        inventory = new AlgorithmInventoryService(c -> productionFilter.include(c) || c == es.urjc.etsii.grafo.mreflp.improve.SequentialImproverNew.class,
                List.of(new GRGraspConstructiveFactory(), new RGGraspConstructiveFactory()),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
        inventory.runComponentDiscovery("es.urjc.etsii");
        generator = new AlgorithmCandidateGenerator(inventory, new DefaultExplorationFilter());
        builder = new AlgorithmBuilderService(inventory);
        space = new AutoconfigSearchSpace(new SolverConfig(), generator);
        automatic = new AutomaticAlgorithmBuilder<>(space, builder);
        compositions = new ComponentSpecJsonCodec().parseList(Files.readString(Path.of("src/test/resources/autoconfig/component-compositions.json")));
    }
    @BeforeEach void setup() { context(1234); }
    @AfterEach void cleanup() { TimeControl.remove(); }

    @Test void bothWavesAndOriginalControlsAreReachableInTheGeneratedSearchSpace() {
        assertTrue(space.snapshot().roots().containsAll(List.of("LMLS", "LMLSNew", "SimpleAlgorithm", "VNS", "IteratedGreedy",
                "ScatterSearch", "ScatterSearchNew", "CMSA", "CMSANew")));
        var names = new HashSet<String>();
        for (var clazz : generator.componentParams().keySet()) names.add(clazz.getSimpleName());
        assertTrue(names.containsAll(List.of("MREFLPConstructive", "OneMoveTabuSearch", "SwapDescent", "MREFLPConstructiveNew",
                "OneMoveDescentNew", "OneMoveTabuSearchNew", "SwapDescentNew", "MREFLPRelocationNeighborhoodNew",
                "MREFLPSwapNeighborhoodNew", "MREFLPMixedNeighborhoodNew", "MREFLPShakeNew", "MREFLPDestructiveNew",
                "MREFLPReconstructiveNew", "MREFLPSimulatedAnnealingNew", "MREFLPSolutionDistanceNew",
                "MREFLPSolutionCombinatorNew", "MREFLPCMSAConstructiveNew", "MREFLPCMSASolverNew", "VND", "SequentialImproverNew")));
        assertFalse(names.contains("LearningMatrixNew"));
        assertFalse(names.contains("LocalSearchCachedBestImprovement"));
        assertFalse(space.iraceParameters().isEmpty());
        System.out.println("MREFLP new roots=" + space.snapshot().roots() + "; generated parameters=" + space.iraceParameters().size());
    }

    @Test void iraceHasOnlyTheSotaStartingConfiguration() throws IOException {
        var seeds = new ComponentSpecJsonCodec().parseList(Files.readString(Path.of("src/main/resources/irace/initial-configurations.json")));
        assertEquals(1, seeds.size());
        var sota = seeds.getFirst();
        assertEquals("LMLS", sota.component());
        assertEquals("LMLS", sota.parameters().get("variant"));
        assertEquals(compositions.getFirst(), sota);
        String table = InitialConfigurationUtil.toIraceTable(space, seeds);
        assertEquals(2, table.split("\n").length);
    }

    @Test void componentCompositionsRoundTripThroughJsonAndIraceToIdenticalSolutions() {
        assertEquals(15, compositions.size());
        String table = InitialConfigurationUtil.toIraceTable(space, compositions);
        assertEquals(16, table.split("\n").length);
        var i = instance(6, 2, 4, 31);
        for (var seed : compositions) {
            context(83);
            var manual = build(seed);
            var expected = manual.algorithm(i);
            new MREFLPSolutionValidator().validate(expected).throwIfFail();
            context(83);
            var rebuilt = withBuilder(automatic.buildFromConfig(flatConfiguration(seed, inventory, generator)));
            var actual = rebuilt.algorithm(i);
            new MREFLPSolutionValidator().validate(actual).throwIfFail();
            assertEquals(expected.cost(), actual.cost(), seed.component());
            assertArrayEquals(expected.assignments(), actual.assignments(), seed.component());
        }
    }

    @Test @Timeout(30) void componentCompositionsRunAcrossSeedsFullOccupancyAndZeroCosts() {
        var instances = List.of(instance(6, 2, 4, 31), instance(6, 2, 3, 9), instance(1, 1, 1, 8),
                new MREFLPInstance("zero", "test", 2, 3, new long[4][4], 0, "fixture"));
        int runs = 0;
        for (var seed : compositions) {
            var algorithm = build(seed);
            for (var i : instances) for (long randomSeed : new long[]{83, 999, 1234}) {
                context(randomSeed);
                var solution = algorithm.algorithm(i);
                new MREFLPSolutionValidator().validate(solution).throwIfFail();
                assertEquals(solution.recalculateCost(), solution.cost());
                runs++;
            }
        }
        assertEquals(180, runs);
    }

    @Test @Timeout(30) void realInstancesAndExpiredBudgetsReturnValidSolutionsPromptly() throws IOException {
        var instances = List.of(MREFLPInstanceUtil.read(Path.of("instances/raw/small/A-10-90.txt"), 2),
                MREFLPInstanceUtil.read(Path.of("instances/raw/medium/A-25-50.txt"), 3),
                MREFLPInstanceUtil.read(Path.of("instances/raw/large/AnKeVa_2005_75dept_set1.txt"), 4),
                MREFLPInstanceUtil.read(Path.of("instances/raw/realworld/tai64c.txt"), 5));
        for (var seed : compositions) {
            var algorithm = build(seed);
            for (var i : instances) {
                context(83);
                TimeControl.setMaxExecutionTime(5, TimeUnit.MILLISECONDS); TimeControl.start();
                long start = System.nanoTime();
                var solution = algorithm.algorithm(i);
                assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(1), seed.component());
                new MREFLPSolutionValidator().validate(solution).throwIfFail();
            }
            context(83);
            TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS); TimeControl.start();
            new MREFLPSolutionValidator().validate(algorithm.algorithm(instances.get(0))).throwIfFail();
        }
    }

    @SuppressWarnings("unchecked")
    private static Algorithm<MREFLPSolution, MREFLPInstance> build(ComponentSpec seed) {
        return withBuilder((Algorithm<MREFLPSolution, MREFLPInstance>) builder.buildAlgorithmFromJson(builder.toJson(seed)));
    }

}
