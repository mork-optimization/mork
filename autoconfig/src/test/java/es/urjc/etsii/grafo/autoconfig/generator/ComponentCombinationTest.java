package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.OrdinalParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComponentCombinationTest {

    private AlgorithmCandidateGenerator candidateGenerator;
    private AutomaticAlgorithmBuilder<TestSolution, TestInstance> algorithmBuilder;

    @BeforeEach
    void setUp() {
        var inventory = new AlgorithmInventoryService.AlgorithmInventory(
                Map.of(
                        Algorithm.class, List.of(ListAlgorithm.class, ArrayAlgorithm.class, ScalarAlgorithm.class),
                        TestElement.class, List.of(ElementA.class, ElementB.class)
                ),
                Map.of(
                        "ListAlgorithm", ListAlgorithm.class,
                        "ArrayAlgorithm", ArrayAlgorithm.class,
                        "ScalarAlgorithm", ScalarAlgorithm.class,
                        "ElementA", ElementA.class,
                        "ElementB", ElementB.class
                ),
                Map.of(),
                Map.of(),
                List.of()
        );
        var inventoryService = mock(AlgorithmInventoryService.class);
        when(inventoryService.getInventory()).thenReturn(inventory);

        candidateGenerator = new AlgorithmCandidateGenerator(inventoryService, new DefaultExplorationFilter());
        algorithmBuilder = new AutomaticAlgorithmBuilder<>(
                new AutoconfigSearchSpace(new SolverConfig(), candidateGenerator),
                new AlgorithmBuilderService(inventoryService)
        );
    }

    @Test
    void storesEligibleCandidatesWithoutExpandingPermutations() {
        var root = findRoot(candidateGenerator.buildTree(4, 2), ListAlgorithm.class);
        var combination = root.combinations().get("elements");
        assertEquals(0, combination.min());
        assertEquals(2, combination.max());
        assertEquals(List.of("ElementA", "ElementB"), choiceNames(combination));
    }

    @Test
    void generatesIndependentConditionalPositionsAndUniquenessConstraints() {
        var space = candidateGenerator.toIraceParameterSpace(candidateGenerator.buildTree(4, 2));
        var params = space.parameters();
        assertTrue(contains(params, "ROOT_ListAlgorithm.elements.length", "(0, 2)", "ROOT %in% c(\"ListAlgorithm\")"));
        for (int position = 0; position < 2; position++) {
            assertTrue(contains(params, "ROOT_ListAlgorithm.elements.item" + position,
                    "(\"ElementA\", \"ElementB\")", "ROOT %in% c(\"ListAlgorithm\")",
                    "ROOT_ListAlgorithm.elements.length >= " + (position + 1)));
        }
        assertTrue(space.forbiddenExpressions().contains("!is.na(ROOT_ListAlgorithm.elements.item0) & "
                + "!is.na(ROOT_ListAlgorithm.elements.item1) & ROOT_ListAlgorithm.elements.item0 == ROOT_ListAlgorithm.elements.item1"));
        for (var parameter : params) assertTrue(!parameter.contains("item0_ElementA.item1"));
    }

    @Test
    void preservesEveryOrderedSelectionAndRejectsDuplicatesAndInvalidActiveValues() {
        var orders = List.of(List.<String>of(), List.of("ElementA"), List.of("ElementB"),
                List.of("ElementA", "ElementB"), List.of("ElementB", "ElementA"));
        for (var order : orders) {
            var parameters = new HashMap<String, String>();
            parameters.put("ROOT", "ListAlgorithm");
            parameters.put("ROOT_ListAlgorithm.elements.length", Integer.toString(order.size()));
            for (int position = 0; position < order.size(); position++) {
                parameters.put("ROOT_ListAlgorithm.elements.item" + position, order.get(position));
            }
            var spec = algorithmBuilder.asComponentSpec(new AlgorithmConfiguration(parameters));
            var expected = new ArrayList<ComponentSpec>();
            for (var component : order) expected.add(new ComponentSpec(component));
            assertEquals(expected, spec.parameters().get("elements"));
        }
        for (var invalid : List.of(
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "3"),
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "-1"),
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "1"),
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "1", "ROOT_ListAlgorithm.elements.item0", "NA"),
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "1", "ROOT_ListAlgorithm.elements.item0", "Unknown"),
                Map.of("ROOT", "ListAlgorithm", "ROOT_ListAlgorithm.elements.length", "2", "ROOT_ListAlgorithm.elements.item0", "ElementA", "ROOT_ListAlgorithm.elements.item1", "ElementA"))) {
            assertThrows(IllegalArgumentException.class, () -> algorithmBuilder.asComponentSpec(new AlgorithmConfiguration(invalid)));
        }
    }

    @Test
    void reconstructsAndBuildsListCombinations() {
        var config = combinationConfig("ListAlgorithm", 2);

        assertEquals(
                new ComponentSpec(
                        "ListAlgorithm",
                        Map.of("elements", List.of(new ComponentSpec("ElementA"), new ComponentSpec("ElementB")))
                ),
                algorithmBuilder.asComponentSpec(config)
        );
        var algorithm = assertInstanceOf(ListAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertEquals(List.of(ElementA.class, ElementB.class), elementClasses(algorithm.elements));
    }

    @Test
    void reconstructsAndBuildsArrayCombinations() {
        var config = combinationConfig("ArrayAlgorithm", 2);

        assertEquals(
                new ComponentSpec(
                        "ArrayAlgorithm",
                        Map.of("elements", List.of(new ComponentSpec("ElementA"), new ComponentSpec("ElementB")))
                ),
                algorithmBuilder.asComponentSpec(config)
        );
        var algorithm = assertInstanceOf(ArrayAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertEquals(List.of(ElementA.class, ElementB.class), elementClasses(List.of(algorithm.elements)));
    }

    @Test
    void supportsTheEmptyCombination() {
        var config = new AlgorithmConfiguration(Map.of(
                "ROOT", "ListAlgorithm",
                "ROOT_ListAlgorithm.elements.length", "0"
        ));

        assertEquals(
                new ComponentSpec("ListAlgorithm", Map.of("elements", List.of())),
                algorithmBuilder.asComponentSpec(config)
        );
        var algorithm = assertInstanceOf(ListAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertTrue(algorithm.elements.isEmpty());
    }

    @Test
    void manualDescriptionsMayRepeatAComponentClass() {
        var algorithm = assertInstanceOf(
                ListAlgorithm.class,
                algorithmBuilder.buildFromJson("""
                        {
                          "$component": "ListAlgorithm",
                          "elements": [
                            {"$component": "ElementA"},
                            {"$component": "ElementA"}
                          ]
                        }
                        """)
        );

        assertEquals(List.of(ElementA.class, ElementA.class), elementClasses(algorithm.elements));
    }

    @Test
    void reconstructsTypedIraceScalarValuesWithoutJsonRoundTrip() {
        var config = new AlgorithmConfiguration(Map.of(
                "ROOT", "ScalarAlgorithm",
                "ROOT_ScalarAlgorithm.iterations", "12",
                "ROOT_ScalarAlgorithm.ratio", "0.25",
                "ROOT_ScalarAlgorithm.label", "\"escaped \\\\ value\"",
                "ROOT_ScalarAlgorithm.symbol", "\"x\""
        ));

        var spec = algorithmBuilder.asComponentSpec(config);
        assertInstanceOf(Integer.class, spec.parameters().get("iterations"));
        assertInstanceOf(Double.class, spec.parameters().get("ratio"));
        assertEquals("escaped \\ value", spec.parameters().get("label"));
        assertEquals("x", spec.parameters().get("symbol"));

        var algorithm = assertInstanceOf(ScalarAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertEquals(12, algorithm.iterations);
        assertEquals(0.25, algorithm.ratio);
        assertEquals("escaped \\ value", algorithm.label);
        assertEquals('x', algorithm.symbol);
    }

    @Test
    void buildsTopElitesInOrderWithStableNames(@TempDir Path temp) throws IOException {
        Path file = temp.resolve("autoconfig-final-elites.json");
        Files.writeString(file, """
                {
                  "elites": [
                    {
                      "configurationId": "17",
                      "parameters": {
                        "ROOT": "ListAlgorithm",
                        "ROOT_ListAlgorithm.elements.length": "0",
                        "ROOT_ListAlgorithm.elements.item0": "NA"
                      }
                    },
                    {
                      "configurationId": "4",
                      "parameters": {
                        "ROOT": "ListAlgorithm",
                        "ROOT_ListAlgorithm.elements.length": "1",
                        "ROOT_ListAlgorithm.elements.item0": "ElementA"
                      }
                    }
                  ]
                }
                """);

        var best = algorithmBuilder.buildTopElites(file.toString(), 1);
        assertEquals(1, best.size());
        assertEquals("irace-17", best.getFirst().getName());
        assertTrue(assertInstanceOf(ListAlgorithm.class, best.getFirst()).elements.isEmpty());

        var all = algorithmBuilder.buildTopElites(file.toString(), 10);
        assertEquals(2, all.size());
        assertEquals("irace-17", all.get(0).getName());
        assertEquals("irace-4", all.get(1).getName());
        assertEquals(List.of(ElementA.class),
                elementClasses(assertInstanceOf(ListAlgorithm.class, all.get(1)).elements));

        assertThrows(IllegalArgumentException.class, () -> algorithmBuilder.buildTopElites(file.toString(), 0));
        assertThrows(IllegalArgumentException.class, () -> algorithmBuilder.buildTopElites(file.toString(), -1));
    }

    private static AlgorithmConfiguration combinationConfig(String root, int length) {
        String collectionPath = "ROOT_" + root + ".elements";
        return new AlgorithmConfiguration(Map.of(
                "ROOT", root,
                collectionPath + ".length", Integer.toString(length),
                collectionPath + ".item0", "ElementA",
                collectionPath + ".item1", "ElementB"
        ));
    }

    private static TreeNode findRoot(List<TreeNode> roots, Class<?> clazz) {
        for (var root : roots) {
            if (root.clazz() == clazz) {
                return root;
            }
        }
        throw new AssertionError("Missing root " + clazz.getSimpleName());
    }

    private static List<String> choiceNames(CombinationTree combination) {
        var names = new ArrayList<String>();
        for (var candidate : combination.candidates()) names.add(candidate.className());
        return names;
    }

    private static boolean contains(List<String> params, String... fragments) {
        for (var parameter : params) {
            boolean matches = true;
            for (var fragment : fragments) {
                if (!parameter.contains(fragment)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    private static List<Class<?>> elementClasses(List<? extends TestElement> elements) {
        var classes = new java.util.ArrayList<Class<?>>();
        for (var element : elements) {
            classes.add(element.getClass());
        }
        return classes;
    }

    public abstract static class TestElement {
    }

    public static class ElementA extends TestElement {
        @AutoconfigConstructor
        public ElementA() {
        }
    }

    public static class ElementB extends TestElement {
        @AutoconfigConstructor
        public ElementB() {
        }
    }

    public static class ListAlgorithm extends Algorithm<TestSolution, TestInstance> {
        private final List<TestElement> elements;

        @AutoconfigConstructor
        public ListAlgorithm(@ComponentParam(max = 2) List<TestElement> elements) {
            super("ListAlgorithm");
            this.elements = elements;
        }

        @Override
        public TestSolution algorithm(TestInstance instance) {
            return null;
        }
    }

    public static class ArrayAlgorithm extends Algorithm<TestSolution, TestInstance> {
        private final TestElement[] elements;

        @AutoconfigConstructor
        public ArrayAlgorithm(@ComponentParam(max = 2) TestElement[] elements) {
            super("ArrayAlgorithm");
            this.elements = elements;
        }

        @Override
        public TestSolution algorithm(TestInstance instance) {
            return null;
        }
    }

    public static class ScalarAlgorithm extends Algorithm<TestSolution, TestInstance> {
        private final int iterations;
        private final double ratio;
        private final String label;
        private final char symbol;

        @AutoconfigConstructor
        public ScalarAlgorithm(
                @IntegerParam(min = 1, max = 100) int iterations,
                @RealParam(min = 0, max = 1) double ratio,
                @OrdinalParam(strings = {"escaped \\ value", "other"}) String label,
                @CategoricalParam(strings = {"x", "y"}) char symbol
        ) {
            super("ScalarAlgorithm");
            this.iterations = iterations;
            this.ratio = ratio;
            this.label = label;
            this.symbol = symbol;
        }

        @Override
        public TestSolution algorithm(TestInstance instance) {
            return null;
        }
    }
}
