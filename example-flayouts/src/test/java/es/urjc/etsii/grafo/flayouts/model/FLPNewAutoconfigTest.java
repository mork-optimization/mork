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
import es.urjc.etsii.grafo.flayouts.improve.FLPVNDNew;
import es.urjc.etsii.grafo.improve.Improver;
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
    private static final Set<String> NEW_COMPONENTS = Set.of("DRFPRandomConstructiveNew", "FLPAddListManagerNew",
            "FLPAddNeighNew", "FLPAddNeighFastNew", "FLPRemoveNeighNew", "FLPSwapNeighNew", "FLPSwapNeighFastNew",
            "FLPRelocateNeighNew", "FLPRelocateNeighFastNew", "FLPOptNeighNew", "FLPOptNeighFastNew",
            "FLPCandidateRelocateNeighNew", "FLPBlockRelocateNeighNew", "FLPFlowConstructiveNew", "FLPRegretConstructiveNew",
            "RandomRemoveDestructiveNew", "FLPRelatedRemoveDestructiveNew", "FLPWorstRemoveDestructiveNew",
            "FLPVNDNew", "FLPSimulatedAnnealingNew", "VNSNew");
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

    @Test
    void allNewComponentsAreAnnotatedReachableAndExportedAndOriginalsRemainAvailable() {
        var rootNames = new HashSet<String>();
        for (var root : space.roots()) rootNames.add(root.className());
        assertEquals(Set.of("SimpleAlgorithm", "IteratedGreedy", "VNS", "VNSNew"), rootNames);
        var reached = new HashSet<String>();
        var visited = Collections.newSetFromMap(new IdentityHashMap<TreeNode, Boolean>());
        for (var root : space.roots()) visit(root, visited, reached);
        for (String name : NEW_COMPONENTS) {
            assertTrue(reached.contains(name), "Unreachable: " + name);
            var clazz = inventory.getInventory().componentByName().get(name);
            assertNotNull(AlgorithmBuilderUtil.findAutoconfigConstructor(clazz), name);
        }
        for (String name : List.of("DRFPRandomConstructive", "FLPAddListManager", "FLPAddNeigh", "FLPRemoveNeigh",
                "FLPSwapNeigh", "FLPRelocateNeigh", "FLPOptNeigh", "RandomRemoveDestructive")) {
            assertTrue(inventory.getInventory().componentByName().containsKey(name), name);
        }
        String parameters = String.join("\n", space.iraceParameters());
        for (String name : NEW_COMPONENTS) assertTrue(parameters.contains(name), name);
        assertFalse(space.iraceParameters().isEmpty());
        System.out.println("FLP new search space: " + space.snapshot().summary());
    }

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
    void everyNewComponentBuildsFromItsGeneratedConstructorAndAlgorithmsExecute() {
        var constructors = List.of(new ComponentSpec("DRFPRandomConstructiveNew"),
                spec("FLPFlowConstructiveNew", "order", "TOTALFLOW", "placement", "ALLPOSITIONS", "randomness", 0.2),
                spec("FLPRegretConstructiveNew", "regretOrder", 3, "fraction", 0.2),
                spec("GreedyRandomGRASPConstructive", "alpha", 0.3, "candidateListManager",
                        spec("FLPAddListManagerNew", "neighborhood", new ComponentSpec("FLPAddNeighNew"))),
                spec("RandomGreedyGRASPConstructive", "alpha", 0.3, "candidateListManager",
                        spec("FLPAddListManagerNew", "neighborhood", new ComponentSpec("FLPAddNeighFastNew"))));
        var neighborhoods = List.of(new ComponentSpec("FLPSwapNeighNew"), new ComponentSpec("FLPSwapNeighFastNew"),
                new ComponentSpec("FLPRelocateNeighNew"), new ComponentSpec("FLPRelocateNeighFastNew"),
                new ComponentSpec("FLPOptNeighNew"), new ComponentSpec("FLPOptNeighFastNew"),
                spec("FLPCandidateRelocateNeighNew", "candidates", 4, "fullScanFrequency", 5, "relation", "MIXED"),
                spec("FLPBlockRelocateNeighNew", "length", 2, "reverse", true, "scope", "ALL"));
        for (var neighborhood : neighborhoods) for (var constructor : constructors) {
            execute(spec("SimpleAlgorithm", "constructive", constructor,
                    "improver", spec("LocalSearchCachedBestImprovement", "neighborhood", neighborhood, "cacheSize", 4)));
        }
        var destructors = List.of(spec("RandomRemoveDestructiveNew", "neighborhood", new ComponentSpec("FLPRemoveNeighNew"), "ratio", 0.25, "scaleWithK", true),
                spec("FLPRelatedRemoveDestructiveNew", "ratio", 0.25, "noise", 0.2, "relation", "FLOW"),
                spec("FLPWorstRemoveDestructiveNew", "ratio", 0.25, "noise", 0.2));
        var vnd = spec("FLPVNDNew", "neighborhoods", List.of(neighborhoods.get(1), neighborhoods.get(3)), "policy", "FIRST");
        var sa = spec("FLPSimulatedAnnealingNew", "neighborhood", neighborhoods.get(1), "acceptance", 0.6,
                "cooling", 0.95, "cycleMultiplier", 1, "maxCycles", 5);
        for (var destructor : destructors) for (var constructor : constructors) {
            var shake = spec("DestroyRebuild", "constructive", constructor, "destructive", destructor);
            execute(spec("IteratedGreedy", "constructive", constructor, "improver", vnd, "destructionReconstruction", shake,
                    "maxIterations", 3, "stopIfNotImprovedIn", 2));
            execute(spec("VNSNew", "constructive", constructor, "improver", sa, "shake", shake, "maxK", 2));
        }
        execute(spec("VNS", "constructive", constructors.getFirst(), "improver", vnd, "maxK", 2,
                "shake", spec("RandomMoveShake", "ratio", 2, "neighborhood", neighborhoods.get(3))));
        execute(spec("SimpleAlgorithm", "constructive", constructors.getFirst(), "improver", sa));
        // Original implementations remain instantiable; their known validator failures are not repaired here.
        for (String original : List.of("DRFPRandomConstructive", "FLPAddListManager", "FLPRemoveNeigh", "FLPSwapNeigh", "FLPOptNeigh")) {
            assertNotNull(builder.buildAlgorithmComponent(new ComponentSpec(original)));
        }
        assertNotNull(builder.buildAlgorithmComponent(spec("FLPRelocateNeigh", "insertBySwap", false)));
        assertNotNull(builder.buildAlgorithmComponent(spec("RandomRemoveDestructive", "ratio", 0.2)));
    }

    @Test
    void enablingRecursionDoesNotExpandTheImproverSpace() throws Exception {
        assertTrue(space.iraceParameters().size() < 3000, "Ordered improver lists must stay within the FLP parameter budget");
        for (int repetitions : List.of(0, 1, 2)) {
            var parameters = generator.toIraceParams(generator.buildTree(1000, repetitions));
            assertEquals(space.iraceParameters(), parameters, "Recursion limit " + repetitions);
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
                if (improver.clazz() == FLPVNDNew.class) {
                    expected = Set.of(FLPSwapNeighFastNew.class, FLPRelocateNeighFastNew.class, FLPOptNeighFastNew.class,
                            FLPCandidateRelocateNeighNew.class, FLPBlockRelocateNeighNew.class);
                    combination = improver.combinations().get("neighborhoods");
                    assertEquals(4, combination.max());
                } else if (improver.clazz() == VND.class || improver.clazz() == Improver.SequentialImprover.class) {
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
                    "LocalSearchCachedBestImprovement", "VND", "SequentialImprover", "FLPVNDNew", "FLPSimulatedAnnealingNew"), names);
        }
    }

    @Test
    void flatIraceListsReconstructAndExecuteAfterPruning() {
        for (String improver : List.of("VND", "SequentialImprover", "FLPVNDNew")) {
            String improverPath = "ROOT_SimpleAlgorithm.improver_" + improver;
            var config = new HashMap<String, String>();
            config.put("ROOT", "SimpleAlgorithm");
            config.put("ROOT_SimpleAlgorithm.constructive", "DRFPRandomConstructiveNew");
            config.put("ROOT_SimpleAlgorithm.improver", improver);
            if (improver.equals("FLPVNDNew")) {
                config.put(improverPath + ".policy", "BEST");
                String path = improverPath + ".neighborhoods";
                config.put(path + ".length", "4");
                path += ".item0";
                config.put(path, "FLPSwapNeighFastNew");
                path += "_FLPSwapNeighFastNew.item1";
                config.put(path, "FLPCandidateRelocateNeighNew");
                path += "_FLPCandidateRelocateNeighNew";
                config.put(path + ".component.candidates", "4");
                config.put(path + ".component.fullScanFrequency", "1");
                config.put(path + ".component.relation", "FLOW");
                path += ".item2";
                config.put(path, "FLPBlockRelocateNeighNew");
                path += "_FLPBlockRelocateNeighNew";
                config.put(path + ".component.length", "2");
                config.put(path + ".component.reverse", "true");
                config.put(path + ".component.scope", "ALL");
                config.put(path + ".item3", "FLPRelocateNeighFastNew");
            } else {
                String path = improverPath + ".improvers";
                config.put(path + ".length", "3");
                path += ".item0";
                config.put(path, "LocalSearchFirstImprovement");
                path += "_LocalSearchFirstImprovement";
                config.put(path + ".component.neighborhood", "FLPSwapNeighFastNew");
                path += ".item1";
                config.put(path, "LocalSearchCachedBestImprovement");
                path += "_LocalSearchCachedBestImprovement";
                config.put(path + ".component.neighborhood", "FLPRelocateNeighFastNew");
                config.put(path + ".component.cacheSize", "4");
                path += ".item2";
                config.put(path, "LocalSearchBestImprovement");
                config.put(path + "_LocalSearchBestImprovement.component.neighborhood", "FLPOptNeighFastNew");
            }
            var automatic = new AutomaticAlgorithmBuilder<FLPSolution, FLPInstance>(space, builder);
            var algorithm = automatic.buildFromConfig(new AlgorithmConfiguration(config));
            FLPNewAlgorithmTest.setBuilder(algorithm);
            assertState(assertTimeout(Duration.ofSeconds(5), () -> algorithm.algorithm(instance(8, 2))), true);
        }
    }

    @Test
    void flatIraceConfigurationReconstructsAndRunsAnAutomaticallyGeneratedAlgorithm() {
        var config = new AlgorithmConfiguration(Map.of(
                "ROOT", "SimpleAlgorithm",
                "ROOT_SimpleAlgorithm.constructive", "DRFPRandomConstructiveNew",
                "ROOT_SimpleAlgorithm.improver", "LocalSearchCachedBestImprovement",
                "ROOT_SimpleAlgorithm.improver_LocalSearchCachedBestImprovement.neighborhood", "FLPRelocateNeighFastNew",
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
