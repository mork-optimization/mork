package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.fill.ObjectiveParamProvider;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.DefaultExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.bmssc.experiment.BlacklistedComponents;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BMSSCNewAutoconfigTest {
    private static final Set<String> NEW_COMPONENTS = Set.of("BMSSCGRASPConstructorNew", "BMSSCListManagerNew",
            "RandomConstructorNew", "RegretConstructor", "SwapNeighborhoodNew", "ShuffledSwapNeighborhood",
            "ThreeCycleNeighborhood", "SampledTwoForTwoNeighborhood", "RandomRemoval", "WorstRemoval", "RelatedRemoval");

    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void discoveryRetainsOriginalComponentsAndTheSameRootAlgorithms() {
        var originalInventory = inventory(false);
        var extendedInventory = inventory(true);
        var originalGenerator = new AlgorithmCandidateGenerator(originalInventory, new DefaultExplorationFilter());
        var extendedGenerator = new AlgorithmCandidateGenerator(extendedInventory, new DefaultExplorationFilter());
        var originalRoots = originalGenerator.buildTree(1000, 0);
        var extendedRoots = extendedGenerator.buildTree(1000, 0);
        assertEquals(Set.of("SimpleAlgorithm", "VNS", "IteratedGreedy", "MultistartOnlyBestAppliesLS"), rootNames(originalRoots));
        assertEquals(rootNames(originalRoots), rootNames(extendedRoots));
        assertTrue(extendedInventory.getInventory().componentByName().keySet().containsAll(originalInventory.getInventory().componentByName().keySet()));
        assertTrue(extendedInventory.getInventory().componentByName().keySet().containsAll(NEW_COMPONENTS));

        var originalReachable = reachable(originalRoots);
        var extendedReachable = reachable(extendedRoots);
        assertTrue(extendedReachable.containsAll(originalReachable));
        for (String name : NEW_COMPONENTS) {
            // The list manager is internal to the GRASP wrapper, rather than a separate tree selector.
            if (!name.equals("BMSSCListManagerNew")) assertTrue(extendedReachable.contains(name), name);
        }
        assertTrue(extendedReachable.containsAll(Set.of("DestroyRebuild", "RandomMoveShake", "VND", "SequentialImprover")));
        assertFalse(extendedReachable.contains("ReassignNeighborhood"));
        assertFalse(extendedReachable.contains("GreedyRandomGRASPConstructive"));
        assertFalse(extendedReachable.contains("RandomGreedyGRASPConstructive"));

        // Export a representative generated branch without expanding every ordered improver combination.
        var ig = find(extendedRoots, "IteratedGreedy");
        var grasp = find(ig.children().get("constructive"), "BMSSCGRASPConstructorNew");
        var destroyRebuild = find(ig.children().get("destructionReconstruction"), "DestroyRebuild");
        var restrictedShake = new TreeNode(destroyRebuild.paramName(), destroyRebuild.clazz(), Map.of(
                "constructive", destroyRebuild.children().get("constructive"),
                "destructive", destroyRebuild.children().get("destructive")), Map.of());
        var cached = find(ig.children().get("improver"), "LocalSearchCachedBestImprovement");
        var branch = new TreeNode(ig.paramName(), ig.clazz(), Map.of("constructive", List.of(grasp),
                "destructionReconstruction", List.of(restrictedShake), "improver", List.of(cached)), Map.of());
        String parameters = String.join("\n", extendedGenerator.toIraceParams(List.of(branch)));
        for (String expected : List.of("RANDOM_GREEDY", "FARTHEST_FIRST", "RegretConstructor", "WorstRemoval",
                "ThreeCycleNeighborhood", "SampledTwoForTwoNeighborhood", "samples")) assertTrue(parameters.contains(expected), expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void selectedComponentsBuildAndExecuteUnderEveryExistingRoot() {
        var builder = new AlgorithmBuilderService(inventory(true));
        var constructors = List.of(new ComponentSpec("RandomConstructorNew"),
                new ComponentSpec("RegretConstructor", Map.of("alpha", 0.4)),
                new ComponentSpec("BMSSCGRASPConstructorNew", Map.of("alpha", 0.6, "strategy", "RANDOM_GREEDY", "seedStrategy", "RANDOM")),
                new ComponentSpec("BMSSCGRASPConstructorNew", Map.of("alpha", 0.6, "strategy", "GREEDY_RANDOM", "seedStrategy", "FARTHEST_FIRST")));
        var neighborhoods = List.of(new ComponentSpec("SwapNeighborhoodNew"), new ComponentSpec("ShuffledSwapNeighborhood"),
                new ComponentSpec("ThreeCycleNeighborhood"), new ComponentSpec("SampledTwoForTwoNeighborhood", Map.of("samples", 16)));
        for (int i = 0; i < constructors.size(); i++) {
            var constructor = constructors.get(i);
            var improver = new ComponentSpec("LocalSearchCachedBestImprovement", Map.of("neighborhood", neighborhoods.get(i), "cacheSize", 4));
            var randomShake = new ComponentSpec("RandomMoveShake", Map.of("ratio", 2, "neighborhood", neighborhoods.get(i)));
            for (String destructor : List.of("RandomRemoval", "WorstRemoval", "RelatedRemoval")) {
                var rebuild = new ComponentSpec("DestroyRebuild", Map.of("constructive", constructor,
                        "destructive", new ComponentSpec(destructor, Map.of("fraction", 0.3))));
                var algorithms = List.of(
                        new ComponentSpec("SimpleAlgorithm", Map.of("constructive", constructor, "improver", improver)),
                        new ComponentSpec("VNS", Map.of("maxK", 2, "constructive", constructor, "improver", improver, "shake", randomShake)),
                        new ComponentSpec("VNS", Map.of("maxK", 2, "constructive", constructor, "improver", improver, "shake", rebuild)),
                        new ComponentSpec("IteratedGreedy", Map.of("maxIterations", 2, "stopIfNotImprovedIn", 2,
                                "constructive", constructor, "improver", improver, "destructionReconstruction", rebuild)),
                        new ComponentSpec("MultistartOnlyBestAppliesLS", Map.of("iterations", 2, "constructor", constructor,
                                "improver", new ComponentSpec("ShakeImprover", Map.of("improver", improver, "shake", rebuild)))));
                for (var spec : algorithms) {
                    var algorithm = (Algorithm<BMSSCSolution, BMSSCInstance>) builder.buildAlgorithm(spec);
                    algorithm.setBuilder(new SolutionBuilder<>() {
                        @Override
                        public BMSSCSolution initializeSolution(BMSSCInstance instance) { return new BMSSCSolution(instance); }
                    });
                    var solution = assertTimeout(Duration.ofSeconds(5), () -> algorithm.algorithm(instance(9, 3)));
                    assertFeasible(solution);
                }
            }
        }
        assertNotNull(builder.buildAlgorithmComponent(new ComponentSpec("BMSSCListManagerNew", Map.of("seedStrategy", "RANDOM"))));
        for (String component : List.of("VND", "SequentialImprover")) {
            var spec = new ComponentSpec(component, Map.of("improvers", List.of(
                    new ComponentSpec("LocalSearchFirstImprovement", Map.of("neighborhood", new ComponentSpec("ThreeCycleNeighborhood"))),
                    new ComponentSpec("LocalSearchBestImprovement", Map.of("neighborhood", new ComponentSpec("ShuffledSwapNeighborhood"))))));
            var improver = (es.urjc.etsii.grafo.improve.Improver<BMSSCSolution, BMSSCInstance>) builder.buildAlgorithmComponent(spec);
            var solution = new es.urjc.etsii.grafo.bmssc.create.RandomConstructorNew().construct(new BMSSCSolution(instance(9, 3)));
            assertFeasible(improver.improve(solution));
        }
    }

    private AlgorithmInventoryService inventory(boolean includeNew) {
        var blacklist = new BlacklistedComponents();
        var inventory = new AlgorithmInventoryService(clazz -> blacklist.include(clazz)
                && (includeNew || !NEW_COMPONENTS.contains(clazz.getSimpleName())), List.of(),
                List.of(new AlgorithmNameParam(), new ObjectiveParamProvider()));
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.algorithms,es.urjc.etsii.grafo.create,es.urjc.etsii.grafo.improve,"
                + "es.urjc.etsii.grafo.shake,es.urjc.etsii.grafo.solution.neighborhood,es.urjc.etsii.grafo.bmssc");
        return inventory;
    }

    private Set<String> rootNames(List<TreeNode> roots) {
        var names = new HashSet<String>();
        for (var root : roots) names.add(root.className());
        return names;
    }

    private Set<String> reachable(List<TreeNode> roots) {
        var names = new HashSet<String>();
        Set<TreeNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var root : roots) collect(root, visited, names);
        return names;
    }

    private void collect(TreeNode node, Set<TreeNode> visited, Set<String> names) {
        if (!visited.add(node)) return;
        names.add(node.className());
        for (var children : node.children().values()) {
            for (var child : children) collect(child, visited, names);
        }
        for (var combination : node.combinations().values()) collectCombination(combination.root(), visited, names);
    }

    private void collectCombination(CombinationNode node, Set<TreeNode> visited, Set<String> names) {
        if (node == null) return;
        for (var choice : node.choices()) {
            collect(choice.component(), visited, names);
            collectCombination(choice.next(), visited, names);
        }
    }

    private TreeNode find(List<TreeNode> nodes, String name) {
        for (var node : nodes) {
            if (node.className().equals(name)) return node;
        }
        throw new AssertionError("Missing generated component: " + name);
    }
}
