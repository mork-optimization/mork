package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationTree;
import es.urjc.etsii.grafo.autoconfig.generator.FilterConfig;
import es.urjc.etsii.grafo.autoconfig.generator.IExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWInsertionSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWVND;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWBackwardViolated;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepair;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TSPTWAutoconfigTest {
    private AlgorithmCandidateGenerator generator;
    private AlgorithmBuilderService builder;
    private AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> automaticBuilder;
    private TreeNode root;

    @BeforeEach
    void setUp() {
        initialize();
        var inventory = new AlgorithmInventoryService(new DefaultInventoryFilter(), List.of(), List.of(new AlgorithmNameParam()));
        // Include framework roots too, to verify that the TSPTW exploration filter excludes them.
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.tsptw,es.urjc.etsii.grafo.algorithms");
        generator = new AlgorithmCandidateGenerator(inventory, new TSPTWExplorationFilter());
        var roots = generator.buildTree(1000, 1);
        assertEquals(1, roots.size());
        root = roots.getFirst();
        assertEquals(GVNS.class, root.clazz());
        builder = new AlgorithmBuilderService(inventory);
        automaticBuilder = new AutomaticAlgorithmBuilder<>(new AutoconfigSearchSpace(new SolverConfig(), generator), builder);
    }

    @AfterEach void tearDown() { cleanup(); }

    @Test
    void springDiscoversTheGvnsFilterAndDisablesTheDefaultFilter() {
        try (var context = new AnnotationConfigApplicationContext()) {
            var scanner = new ClassPathBeanDefinitionScanner(context, false);
            // Use the same inherited-component filter as Mork's application scan.
            scanner.addIncludeFilter(new AnnotationTypeFilter(InheritedComponent.class));
            scanner.scan(TSPTWExplorationFilter.class.getPackageName());
            context.register(FilterConfig.class);
            context.refresh();
            var filters = context.getBeansOfType(IExplorationFilter.class);
            assertEquals(1, filters.size());
            assertInstanceOf(TSPTWExplorationFilter.class, filters.values().iterator().next());
        }
    }

    @Test
    void generatesAndBuildsAll96ConfigurationsIncludingJsonRoundTrips() throws Exception {
        var constructive = root.children().get("constructive").getFirst();
        var repair = constructive.children().get("repair").getFirst();
        var localSearch = root.children().get("localSearch").getFirst();
        var repairOrders = combinations(repair.combinations().get("phases"));
        var searchOrders = combinations(localSearch.combinations().get("searches"));
        assertEquals(24, repairOrders.size());
        assertEquals(4, searchOrders.size());
        Set<String> specs = new HashSet<>();
        var instance = instance("best");
        for (var phases : repairOrders) {
            for (var searches : searchOrders) {
                var config = config(phases, searches, 8, 31);
                var spec = automaticBuilder.asComponentSpec(config);
                var algorithm = assertInstanceOf(GVNS.class, automaticBuilder.buildFromConfig(config));
                var json = builder.toJson(spec);
                assertTrue(specs.add(json));
                var rebuilt = assertInstanceOf(GVNS.class, builder.buildAlgorithmFromJson(json));
                assertEquals(algorithm.toString(), rebuilt.toString());
                expireTimeLimit();
                assertConsistent(rebuilt.algorithm(instance));
            }
        }
        assertEquals(96, specs.size());
    }

    @Test
    void hasBoundedNumericParametersAndOriginalDefaultGraph() throws Exception {
        var parameters = String.join("\n", generator.toIraceParams(List.of(root)));
        assertTrue(parameters.contains("ROOT_GVNS.levelMax"));
        assertTrue(parameters.contains("(2, 16)"));
        assertTrue(parameters.contains("ROOT_GVNS.attemptsPerLevel"));
        assertTrue(parameters.contains("(1, 100)"));
        assertFalse(parameters.contains("algorithmName"));
        assertFalse(parameters.contains("ExampleTSPTWShake"));
        assertFalse(parameters.contains("TSPTWListManager"));
        assertFalse(parameters.contains("level_max"));

        var phases = List.of("TSPTWBackwardViolated", "TSPTWForwardNonviolated", "TSPTWForwardViolated", "TSPTWBackwardNonviolated");
        var searches = List.of("TSPTWInsertionSearch", "TSPTWTwoOptSearch");
        var defaults = automaticBuilder.buildFromConfig(config(phases, searches, 8, 31));
        assertEquals(new GVNS().toString(), defaults.toString());
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/gvns-default.json"))) {
            var documented = builder.buildAlgorithmFromJson(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals(defaults.toString(), documented.toString());
        }
        for (int level : new int[]{2, 16}) {
            for (int attempts : new int[]{1, 100}) {
                assertInstanceOf(GVNS.class, automaticBuilder.buildFromConfig(config(phases, searches, level, attempts)));
            }
        }
    }

    @Test
    void rejectsInvalidManualListsAndIncompatibleShake() {
        assertThrows(IllegalArgumentException.class, () -> new TSPTWVND(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWVND(List.of(new TSPTWInsertionSearch(), new TSPTWInsertionSearch())));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibilityRepair(List.of(new TSPTWBackwardViolated())));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibilityRepair(List.of(
                new TSPTWBackwardViolated(), new TSPTWBackwardViolated(), new TSPTWBackwardViolated(), new TSPTWBackwardViolated())));
        assertThrows(IllegalArgumentException.class, () -> new GVNS("invalid", 1, 31,
                new TSPTWFeasibleConstructive(), new TSPTWFeasibleInsertShake(), new TSPTWVND()));
        assertThrows(IllegalArgumentException.class, () -> new GVNS("invalid", 8, 0,
                new TSPTWFeasibleConstructive(), new TSPTWFeasibleInsertShake(), new TSPTWVND()));
        var valid = automaticBuilder.asComponentSpec(config(
                List.of("TSPTWBackwardViolated", "TSPTWForwardNonviolated", "TSPTWForwardViolated", "TSPTWBackwardNonviolated"),
                List.of("TSPTWInsertionSearch", "TSPTWTwoOptSearch"), 8, 31));
        assertInstanceOf(GVNS.class, builder.buildAlgorithm(valid));
        var parameters = new HashMap<>(valid.parameters());
        parameters.put("shake", new ComponentSpec("TSPTWUnrestrictedInsertShake"));
        var invalid = new ComponentSpec("GVNS", parameters);
        assertThrows(RuntimeException.class, () -> builder.buildAlgorithm(invalid));
        assertEquals(List.of(TSPTWFeasibleInsertShake.class), classes(root.children().get("shake")));
    }

    private static List<Class<?>> classes(List<TreeNode> nodes) {
        var result = new ArrayList<Class<?>>();
        for (var node : nodes) result.add(node.clazz());
        return result;
    }

    private static List<List<String>> combinations(CombinationTree tree) {
        var result = new ArrayList<List<String>>();
        collect(tree.root(), tree.min(), new ArrayList<>(), result);
        return result;
    }

    private static void collect(CombinationNode node, int min, List<String> prefix, List<List<String>> result) {
        if (prefix.size() >= min) result.add(List.copyOf(prefix));
        if (node == null) return;
        for (var choice : node.choices()) {
            prefix.add(choice.component().className());
            collect(choice.next(), min, prefix, result);
            prefix.removeLast();
        }
    }

    private static AlgorithmConfiguration config(List<String> phases, List<String> searches, int levelMax, int attempts) {
        var params = new HashMap<String, String>();
        params.put("ROOT", "GVNS");
        params.put("ROOT_GVNS.levelMax", Integer.toString(levelMax));
        params.put("ROOT_GVNS.attemptsPerLevel", Integer.toString(attempts));
        params.put("ROOT_GVNS.constructive", "TSPTWFeasibleConstructive");
        String constructive = "ROOT_GVNS.constructive_TSPTWFeasibleConstructive";
        params.put(constructive + ".initial", "TSPTWRandomConstructive");
        params.put(constructive + ".repair", "TSPTWFeasibilityRepair");
        params.put(constructive + ".perturbation", "TSPTWUnrestrictedInsertShake");
        addCombination(params, constructive + ".repair_TSPTWFeasibilityRepair.phases", phases);
        params.put("ROOT_GVNS.shake", "TSPTWFeasibleInsertShake");
        params.put("ROOT_GVNS.localSearch", "TSPTWVND");
        String searchPath = "ROOT_GVNS.localSearch_TSPTWVND.searches";
        params.put(searchPath + ".length", Integer.toString(searches.size()));
        addCombination(params, searchPath, searches);
        return new AlgorithmConfiguration(params);
    }

    private static void addCombination(Map<String, String> params, String path, List<String> components) {
        for (int i = 0; i < components.size(); i++) {
            String selector = path + ".item" + i;
            params.put(selector, components.get(i));
            path = selector + "_" + components.get(i);
        }
    }
}
