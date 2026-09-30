package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.autoconfig.builder.*;
import es.urjc.etsii.grafo.autoconfig.factories.*;
import es.urjc.etsii.grafo.autoconfig.fill.*;
import es.urjc.etsii.grafo.autoconfig.generator.*;
import es.urjc.etsii.grafo.autoconfig.inventory.*;
import es.urjc.etsii.grafo.autoconfig.irace.*;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.flayouts.autoconfig.FLPExplorationFilterNew;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPNewAutoconfigTest {
    private static final Set<String> TUNABLE_COMPONENTS = Set.of("FLPRandomConstructiveNew", "FLPAddListManagerNew",
            "FLPAddNeigh", "FLPRemoveNeighNew", "FLPSwapNeighNew",
            "FLPRelocateNeighNew", "FLPOptNeighNew",
            "FLPCandidateRelocateNeighNew", "FLPBlockRelocateNeighNew", "FLPFlowConstructiveNew", "FLPRegretConstructiveNew",
            "RandomRemoveDestructiveNew", "FLPRelatedRemoveDestructiveNew", "FLPWorstRemoveDestructiveNew",
            "FLPSimulatedAnnealingNew");
    private static AlgorithmInventoryService inventory;
    private static AlgorithmBuilderService builder;
    private static AlgorithmCandidateGenerator generator;
    private static AutoconfigSearchSpace space;

    @BeforeAll
    static void buildSpace() {
        // Use the same inherited-component scan as Mork, including static nested baseline factories.
        try (var context = new AnnotationConfigApplicationContext()) {
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(InheritedComponent.class));
            scanner.scan("es.urjc.etsii.grafo.flayouts.autoconfig");
            context.refresh();
            var factories = new ArrayList<>(context.getBeansOfType(AlgorithmComponentFactory.class).values());
            assertEquals(7, factories.size());
            factories.add(new GRGraspConstructiveFactory());
            factories.add(new RGGraspConstructiveFactory());
            inventory = new AlgorithmInventoryService(new DefaultInventoryFilter(), factories,
                    List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
            inventory.runComponentDiscovery("es.urjc.etsii");
            generator = new AlgorithmCandidateGenerator(inventory, context.getBean(FLPExplorationFilterNew.class));
            var config = new SolverConfig();
            config.setTreeDepth(1000);
            config.setMaxDerivationRepetition(0);
            space = new AutoconfigSearchSpace(config, generator);
            builder = new AlgorithmBuilderService(inventory);
        }
    }

    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    private static void visit(TreeNode node, Set<TreeNode> visited, Set<String> reached) {
        if (!visited.add(node)) return;
        reached.add(node.className());
        for (var list : node.children().values()) for (var child : list) {
            if (node.clazz() == LocalSearchCachedBestImprovement.class && Neighborhood.class.isAssignableFrom(child.clazz())) {
                assertTrue(FLPPreservingNeighNew.class.isAssignableFrom(child.clazz()), "Cached LS needs refreshable preserving moves");
            }
            visit(child, visited, reached);
        }
        for (var combination : node.combinations().values()) visitCombination(combination.root(), visited, reached);
    }

    private static void visitCombination(CombinationNode node, Set<TreeNode> visited, Set<String> reached) {
        if (node == null) return;
        for (var choice : node.choices()) {
            visit(choice.component(), visited, reached);
            visitCombination(choice.next(), visited, reached);
        }
    }

    @Test
    void originalRelocationExecutesThroughTheAutoconfigFactory() {
        for (String search : List.of("LocalSearchFirstImprovement", "LocalSearchBestImprovement")) {
            execute(spec("SimpleAlgorithm", "constructive", new ComponentSpec("FLPRandomConstructive"),
                    "improver", spec(search, "neighborhood", new ComponentSpec("FLPRelocateNeigh"))));
        }
    }

    @Test
    void enablingRecursionDoesNotExpandTheImproverSpace() throws Exception {
        assertTrue(space.iraceParameters().size() < 2677, "Consolidation must reduce the previous 2677 parameter definitions");
        for (int repetitions : List.of(0, 1, 2)) {
            var parameters = generator.toIraceParams(generator.buildTree(1000, repetitions));
            assertEquals(space.iraceParameters(), parameters, "Recursion limit " + repetitions);
            for (var parameter : parameters) {
                assertFalse(parameter.contains("SequentialImprover"), "SequentialImprover must be excluded at every depth");
            }
            System.out.println("FLP parameters with max-derivation-repetition=" + repetitions + ": " + parameters.size());
        }
        var output = Path.of("target", "autoconfig-space", "parameters.txt");
        Files.createDirectories(output.getParent());
        Files.writeString(output, String.join("\n", space.iraceParameters()) + "\n\n[global]\ndigits = 2\n");
    }

    @Test
    void composedImproversKeepBoundedDistinctChoicesAndAllTopLevelAlternatives() {
        for (var root : space.roots()) {
            var improvers = root.children().get("improver");
            var names = new HashSet<String>();
            for (var improver : improvers) {
                names.add(improver.className());
                Set<Class<?>> expected;
                CombinationTree combination;
                if (improver.clazz() == VND.class) {
                    expected = Set.of(LocalSearchFirstImprovement.class, LocalSearchBestImprovement.class,
                            LocalSearchCachedBestImprovement.class);
                    combination = improver.combinations().get("improvers");
                    assertEquals(3, combination.max());
                } else {
                    continue;
                }
                assertEquals(2, combination.min());
                assertEquals(expected.size(), combination.root().choices().size());
                for (var choice : combination.root().choices()) assertTrue(expected.contains(choice.component().clazz()));
            }
            assertEquals(Set.of("NullImprover", "LocalSearchFirstImprovement", "LocalSearchBestImprovement",
                    "LocalSearchCachedBestImprovement", "VND", "FLPSimulatedAnnealingNew"), names);
        }
    }

    @Test
    void flatIraceVndListReconstructsAndExecutesAfterPruning() {
        var config = new HashMap<String, String>();
        config.put("ROOT", "SimpleAlgorithm");
        config.put("ROOT_SimpleAlgorithm.constructive", "FLPRandomConstructiveNew");
        config.put("ROOT_SimpleAlgorithm.improver", "VND");

        String path = "ROOT_SimpleAlgorithm.improver_VND.improvers";
        config.put(path + ".length", "3");
        path += ".item0";
        config.put(path, "LocalSearchFirstImprovement");
        path += "_LocalSearchFirstImprovement";
        config.put(path + ".component.neighborhood", "FLPSwapNeighNew");
        path += ".item1";
        config.put(path, "LocalSearchCachedBestImprovement");
        path += "_LocalSearchCachedBestImprovement";
        config.put(path + ".component.neighborhood", "FLPRelocateNeighNew");
        config.put(path + ".component.cacheSize", "4");
        path += ".item2";
        config.put(path, "LocalSearchBestImprovement");
        config.put(path + "_LocalSearchBestImprovement.component.neighborhood", "FLPOptNeighNew");

        var automatic = new AutomaticAlgorithmBuilder<FLPSolution, FLPInstance>(space, builder);
        var algorithm = automatic.buildFromConfig(new AlgorithmConfiguration(config));
        FLPNewAlgorithmTest.setBuilder(algorithm);
        assertState(assertTimeout(Duration.ofSeconds(5), () -> algorithm.algorithm(instance(8, 2))), true);
    }

    @Test
    void flatIraceConfigurationReconstructsAndRunsAnAutomaticallyGeneratedAlgorithm() {
        var config = new AlgorithmConfiguration(Map.of(
                "ROOT", "SimpleAlgorithm",
                "ROOT_SimpleAlgorithm.constructive", "FLPRandomConstructiveNew",
                "ROOT_SimpleAlgorithm.improver", "LocalSearchCachedBestImprovement",
                "ROOT_SimpleAlgorithm.improver_LocalSearchCachedBestImprovement.neighborhood", "FLPRelocateNeighNew",
                "ROOT_SimpleAlgorithm.improver_LocalSearchCachedBestImprovement.cacheSize", "4"));
        var automatic = new AutomaticAlgorithmBuilder<FLPSolution, FLPInstance>(space, builder);
        var algorithm = automatic.buildFromConfig(config);
        FLPNewAlgorithmTest.setBuilder(algorithm);
        assertState(algorithm.algorithm(instance(10, 2)), true);
    }

    @SuppressWarnings("unchecked")
    private void execute(ComponentSpec spec) {
        var algorithm = (Algorithm<FLPSolution, FLPInstance>) builder.buildAlgorithm(spec);
        FLPNewAlgorithmTest.setBuilder(algorithm);
        assertState(assertTimeout(Duration.ofSeconds(5), () -> algorithm.algorithm(instance(8, 2))), true);
    }

    private static ComponentSpec spec(String component, Object... pairs) {
        var params = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) params.put((String) pairs[i], pairs[i + 1]);
        return new ComponentSpec(component, params);
    }
}
