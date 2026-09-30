package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.fill.AlgorithmNameParam;
import es.urjc.etsii.grafo.autoconfig.generator.*;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.inventory.DefaultInventoryFilter;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolutionValidator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(30)
class TSPTWNewAutoconfigTest {
    private AlgorithmCandidateGenerator generator;
    private AlgorithmBuilderService builder;
    private AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> automatic;
    private TreeNode root;

    @BeforeEach void setUp() {
        initialize();
        configureSpace(true);
    }

    private void configureSpace(boolean legacy) {
        var inventory = new AlgorithmInventoryService(new DefaultInventoryFilter(), List.of(), List.of(new AlgorithmNameParam()));
        inventory.runComponentDiscovery("es.urjc.etsii.grafo.tsptw,es.urjc.etsii.grafo.algorithms");
        generator = new AlgorithmCandidateGenerator(inventory, new TSPTWExplorationFilter(legacy));
        var roots = generator.buildTree(1000, 1);
        assertEquals(1, roots.size());
        root = roots.getFirst();
        assertEquals(GVNS.class, root.clazz());
        builder = new AlgorithmBuilderService(inventory);
        automatic = new AutomaticAlgorithmBuilder<>(new AutoconfigSearchSpace(new SolverConfig(), generator), builder);
    }

    @AfterEach void tearDown() { cleanup(); }

    @Test
    void exposesAllEightVariantsThroughTheOriginalRootAndBuildsEveryOrderedSubcomposition() {
        var constructive = child(root, "constructive", "TSPTWFeasibleConstructive");
        assertEquals(2, constructive.children().get("initial").size());
        assertEquals(4, constructive.children().get("repair").size());
        assertEquals(2, root.children().get("shake").size());
        assertEquals(2, root.children().get("localSearch").size());
        var oldRepairs = combinations(child(constructive, "repair", "TSPTWFeasibilityRepair").combinations().get("phases"));
        var newRepairs = combinations(child(constructive, "repair", "TSPTWFeasibilityRepairNew").combinations().get("phases"));
        var oldSearches = combinations(child(root, "localSearch", "TSPTWVND").combinations().get("searches"));
        var newSearches = combinations(child(root, "localSearch", "TSPTWVNDNew").combinations().get("searches"));
        assertEquals(24, oldRepairs.size());
        assertEquals(64, newRepairs.size());
        assertEquals(36, oldSearches.size());
        assertEquals(120, newSearches.size());

        var repairs = new ArrayList<>(oldRepairs);
        repairs.addAll(newRepairs);
        var searches = new ArrayList<>(oldSearches);
        searches.addAll(newSearches);
        var seenVariants = new HashSet<String>();
        for (int i = 0; i < searches.size(); i++) {
            int repairIndex = i % repairs.size();
            for (int initial = 0; initial < 2; initial++) {
                for (int shake = 0; shake < 2; shake++) {
                    var spec = specification(initial == 1, shake == 1, repairIndex >= oldRepairs.size(),
                            i >= oldSearches.size(), repairs.get(repairIndex), searches.get(i), i);
                    var params = new HashMap<String, String>();
                    flatten("ROOT", spec, params, seenVariants);
                    var config = new AlgorithmConfiguration(params);
                    var direct = builder.buildAlgorithm(spec);
                    var built = automatic.buildFromConfig(config);
                    assertEquals(direct.toString(), built.toString());
                    var rebuilt = builder.buildAlgorithmFromJson(automatic.asJson(config));
                    assertEquals(built.toString(), rebuilt.toString());
                }
            }
        }
        assertEquals(Set.of("TSPTWRandomConstructiveNew", "TSPTWFeasibleInsertShakeNew", "TSPTWFeasibilityRepairNew",
                "TSPTWVNDNew", "TSPTWInsertionSearchNew", "TSPTWTwoOptSearchNew", "TSPTWSwapSearchNew", "TSPTWOrOptSearchNew"), seenVariants);
        String parameters = String.join("\n", generator.toIraceParams(List.of(root)));
        for (String name : List.of("selection", "urgencyWeight", "candidateListSize", "attemptFactor", "maxPasses", "blockLength")) {
            assertTrue(parameters.contains(name));
        }
        assertTrue(parameters.contains("FIRST"));
        assertTrue(parameters.contains("BEST"));
        assertFalse(parameters.contains("ExampleTSPTWShake"));
        assertFalse(parameters.contains("TSPTWListManager"));
    }

    @Test
    void defaultSpaceBuildsAll1248StructuresWithOnlyTheFixedRepairBundle() {
        configureSpace(false);
        assertEquals(2, root.children().get("constructive").size());
        for (var constructive : root.children().get("constructive")) {
            assertEquals(2, constructive.children().get("initial").size());
            var repairs = constructive.children().get("repair");
            assertEquals(1, repairs.size());
            assertEquals("TSPTWFeasibilityRepairFullNew", repairs.getFirst().className());
            assertTrue(repairs.getFirst().children().isEmpty());
            assertTrue(repairs.getFirst().combinations().isEmpty());
        }
        var oldSearches = combinations(child(root, "localSearch", "TSPTWVND").combinations().get("searches"));
        var newSearches = combinations(child(root, "localSearch", "TSPTWVNDNew").combinations().get("searches"));
        var searches = new ArrayList<>(oldSearches);
        searches.addAll(newSearches);
        var configurations = new HashSet<String>();
        for (var constructive : root.children().get("constructive")) {
            for (int initial = 0; initial < 2; initial++) {
                for (int shake = 0; shake < 2; shake++) {
                    for (int i = 0; i < searches.size(); i++) {
                        var spec = fullSpecification(constructive.className(), initial == 1, shake == 1,
                                i >= oldSearches.size(), searches.get(i), i);
                        var params = new HashMap<String, String>();
                        flatten("ROOT", spec, params, new HashSet<>());
                        var config = new AlgorithmConfiguration(params);
                        var built = automatic.buildFromConfig(config);
                        assertEquals(builder.buildAlgorithm(spec).toString(), built.toString());
                        var json = automatic.asJson(config);
                        assertTrue(configurations.add(json));
                        assertEquals(built.toString(), builder.buildAlgorithmFromJson(json).toString());
                    }
                }
            }
        }
        assertEquals(1248, configurations.size());
        String parameters = String.join("\n", generator.toIraceParams(List.of(root)));
        assertTrue(parameters.contains("TSPTWFeasibilityRepairFullNew"));
        assertTrue(parameters.contains("TSPTWFeasibleConstructiveBestNew"));
        for (String hidden : List.of(".phases", "maxPasses", "TSPTWFeasibilityRepairNew",
                "TSPTWFeasibilityRepairFallbackNew", "TSPTWBackwardViolated", "TSPTWForwardNonviolated",
                "TSPTWForwardViolated", "TSPTWBackwardNonviolated")) {
            assertFalse(parameters.contains(hidden), hidden);
        }
    }

    @Test
    void defaultGraphsExecuteWithBothConstructivesAndInitialConstructors() throws Exception {
        configureSpace(false);
        enableMetrics();
        for (var constructive : root.children().get("constructive")) {
            for (int initial = 0; initial < 2; initial++) {
                var spec = fullSpecification(constructive.className(), initial == 1, true, true,
                        List.of("TSPTWInsertionSearchNew", "TSPTWTwoOptSearchNew", "TSPTWOrOptSearchNew"), 0);
                var params = new HashMap<String, String>();
                flatten("ROOT", spec, params, new HashSet<>());
                var algorithm = automatic.buildFromConfig(new AlgorithmConfiguration(params));
                Metrics.resetMetrics();
                TimeControl.setMaxExecutionTime(10, TimeUnit.MILLISECONDS);
                TimeControl.start();
                var result = algorithm.algorithm(instance("best"));
                assertConsistent(result);
                assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
                assertFalse(Metrics.get("Cost").getValues().isEmpty());
            }
        }
    }

    @Test
    void fallbackRepairIsIndependentlySelectableWithEverySubsetAndRoundTripsThroughIrace() {
        var constructive = child(root, "constructive", "TSPTWFeasibleConstructive");
        var fallback = child(constructive, "repair", "TSPTWFeasibilityRepairFallbackNew");
        var subsets = combinations(fallback.combinations().get("phases"));
        assertEquals(64, subsets.size());
        for (var phases : subsets) {
            var spec = specification(false, false, true, false, phases,
                    List.of("TSPTWInsertionSearch"), 2);
            var params = new HashMap<String, String>();
            flatten("ROOT", spec, params, new HashSet<>());
            var fallbackParams = new HashMap<String, String>();
            for (var entry : params.entrySet()) {
                fallbackParams.put(entry.getKey().replace("TSPTWFeasibilityRepairNew", "TSPTWFeasibilityRepairFallbackNew"),
                        entry.getValue().replace("TSPTWFeasibilityRepairNew", "TSPTWFeasibilityRepairFallbackNew"));
            }
            var config = new AlgorithmConfiguration(fallbackParams);
            var built = automatic.buildFromConfig(config);
            assertTrue(built.toString().contains("TSPTWFeasibilityRepairFallbackNew"));
            assertEquals(built.toString(), builder.buildAlgorithmFromJson(automatic.asJson(config)).toString());
        }
    }

    @Test
    void mixedOriginalAndNewGraphsExecuteAndPassTheValidator() throws Exception {
        var instance = instance("best");
        enableMetrics();
        for (int mask = 0; mask < 16; mask++) {
            var phases = List.of("TSPTWBackwardViolated", "TSPTWForwardNonviolated", "TSPTWForwardViolated", "TSPTWBackwardNonviolated");
            boolean newVnd = (mask & 8) != 0;
            var searches = newVnd ? List.of("TSPTWInsertionSearchNew", "TSPTWOrOptSearchNew", "TSPTWSwapSearchNew")
                    : List.of("TSPTWInsertionSearch", "TSPTWTwoOptSearchNew");
            var spec = specification((mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, newVnd, phases, searches, mask);
            var algorithm = assertInstanceOf(GVNS.class, builder.buildAlgorithm(spec));
            Metrics.resetMetrics();
            TimeControl.setMaxExecutionTime(10, TimeUnit.MILLISECONDS);
            TimeControl.start();
            var result = algorithm.algorithm(instance);
            assertConsistent(result);
            assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
            assertFalse(Metrics.get("Cost").getValues().isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/gvns-new.json", "/gvns-repair-fallback-new.json", "/gvns-full-repair-new.json"})
    void documentedNewGraphBuildsAndExecutes(String resource) throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream(resource))) {
            var algorithm = assertInstanceOf(GVNS.class, builder.buildAlgorithmFromJson(new String(input.readAllBytes(), StandardCharsets.UTF_8)));
            assertTrue(algorithm.toString().contains("TSPTWVNDNew"));
            TimeControl.setMaxExecutionTime(10, TimeUnit.MILLISECONDS);
            TimeControl.start();
            var result = algorithm.algorithm(instance("best"));
            assertConsistent(result);
            assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
        }
    }

    private static TreeNode child(TreeNode parent, String field, String name) {
        for (var node : parent.children().get(field)) if (node.className().equals(name)) return node;
        throw new AssertionError("Missing component " + name);
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

    private static ComponentSpec specification(boolean newInitial, boolean newShake, boolean newRepair, boolean newVnd,
                                               List<String> phases, List<String> searches, int variant) {
        var phaseSpecs = new ArrayList<ComponentSpec>();
        for (var phase : phases) phaseSpecs.add(new ComponentSpec(phase));
        var searchSpecs = new ArrayList<ComponentSpec>();
        for (var search : searches) {
            Map<String, Object> params = switch (search) {
                case "TSPTWInsertionSearchNew" -> Map.of("selection", variant % 2 == 0 ? "FIRST" : "BEST");
                case "TSPTWOrOptSearchNew" -> Map.of("blockLength", 2 + variant % 2);
                default -> Map.of();
            };
            searchSpecs.add(new ComponentSpec(search, params));
        }
        var repair = new ComponentSpec(newRepair ? "TSPTWFeasibilityRepairNew" : "TSPTWFeasibilityRepair",
                newRepair ? Map.of("phases", phaseSpecs, "maxPasses", variant % 9) : Map.of("phases", phaseSpecs));
        return new ComponentSpec("GVNS", Map.of("levelMax", 2 + variant % 15, "attemptsPerLevel", 1 + variant % 100,
                "constructive", new ComponentSpec("TSPTWFeasibleConstructive", Map.of(
                        "initial", newInitial ? new ComponentSpec("TSPTWRandomConstructiveNew", Map.of(
                                "urgencyWeight", variant % 3 / 2.0, "candidateListSize", 1 + variant % 8)) : new ComponentSpec("TSPTWRandomConstructive"),
                        "repair", repair, "perturbation", new ComponentSpec("TSPTWUnrestrictedInsertShake"))),
                "shake", newShake ? new ComponentSpec("TSPTWFeasibleInsertShakeNew", Map.of("attemptFactor", 2 + variant % 9)) : new ComponentSpec("TSPTWFeasibleInsertShake"),
                "localSearch", new ComponentSpec(newVnd ? "TSPTWVNDNew" : "TSPTWVND", Map.of("searches", searchSpecs))));
    }

    private static ComponentSpec fullSpecification(String constructive, boolean newInitial, boolean newShake,
                                                   boolean newVnd, List<String> searches, int variant) {
        var original = specification(newInitial, newShake, false, newVnd, List.of(), searches, variant);
        var params = new HashMap<>(original.parameters());
        var construction = new HashMap<>(((ComponentSpec) params.get("constructive")).parameters());
        construction.put("repair", new ComponentSpec("TSPTWFeasibilityRepairFullNew"));
        params.put("constructive", new ComponentSpec(constructive, construction));
        return new ComponentSpec("GVNS", params);
    }

    private static void flatten(String selector, ComponentSpec spec, Map<String, String> params, Set<String> seen) {
        params.put(selector, spec.component());
        flattenParameters(selector + "_" + spec.component(), spec, params, seen);
    }

    private static void flattenParameters(String path, ComponentSpec spec, Map<String, String> params, Set<String> seen) {
        if (spec.component().endsWith("New")) seen.add(spec.component());
        for (var entry : spec.parameters().entrySet()) {
            String field = path + "." + entry.getKey();
            if (entry.getValue() instanceof ComponentSpec child) {
                flatten(field, child, params, seen);
            } else if (entry.getValue() instanceof List<?> list) {
                params.put(field + ".length", Integer.toString(list.size()));
                for (int i = 0; i < list.size(); i++) {
                    var child = (ComponentSpec) list.get(i);
                    String selector = field + ".item" + i;
                    params.put(selector, child.component());
                    field = selector + "_" + child.component();
                    flattenParameters(field + ".component", child, params, seen);
                }
            } else {
                params.put(field, entry.getValue().toString());
            }
        }
    }
}
