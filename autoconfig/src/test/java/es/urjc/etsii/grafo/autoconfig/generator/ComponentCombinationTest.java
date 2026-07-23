package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
                        Algorithm.class, List.of(ListAlgorithm.class, ArrayAlgorithm.class),
                        TestElement.class, List.of(ElementA.class, ElementB.class)
                ),
                Map.of(
                        "ListAlgorithm", ListAlgorithm.class,
                        "ArrayAlgorithm", ArrayAlgorithm.class,
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
                new SolverConfig(),
                candidateGenerator,
                new AlgorithmBuilderService(inventoryService)
        );
    }

    @Test
    void buildsAllOrderedCombinationsWithoutRepetition() {
        var root = findRoot(candidateGenerator.buildTree(4, 2), ListAlgorithm.class);
        var combination = root.combinations().get("elements");

        assertEquals(0, combination.min());
        assertEquals(2, combination.max());
        assertEquals(List.of("ElementA", "ElementB"), choiceNames(combination.root()));

        var afterA = combination.root().choices().get(0).next();
        var afterB = combination.root().choices().get(1).next();
        assertEquals(List.of("ElementB"), choiceNames(afterA));
        assertEquals(List.of("ElementA"), choiceNames(afterB));
    }

    @Test
    void generatesConditionalPrefixParametersForIrace() {
        var params = candidateGenerator.toIraceParams(candidateGenerator.buildTree(4, 2));

        assertTrue(contains(params,
                "ROOT_ListAlgorithm.elements.length",
                "(0, 2)",
                "ROOT %in% c(\"ListAlgorithm\")"));
        assertTrue(contains(params,
                "ROOT_ListAlgorithm.elements.item0",
                "(\"ElementA\", \"ElementB\")",
                " & ",
                "ROOT_ListAlgorithm.elements.length >= 1"));
        assertTrue(contains(params,
                "ROOT_ListAlgorithm.elements.item0_ElementA.item1",
                "(\"ElementB\")",
                "ROOT_ListAlgorithm.elements.item0 %in% c(\"ElementA\")",
                " & ",
                "ROOT_ListAlgorithm.elements.length >= 2"));
        assertTrue(contains(params,
                "ROOT_ListAlgorithm.elements.item0_ElementB.item1",
                "(\"ElementA\")",
                "ROOT_ListAlgorithm.elements.item0 %in% c(\"ElementB\")",
                "ROOT_ListAlgorithm.elements.length >= 2"));
    }

    @Test
    void reconstructsAndBuildsListCombinations() {
        var config = combinationConfig("ListAlgorithm", 2);

        assertEquals(
                "ListAlgorithm{elements=[ElementA{},ElementB{}]}",
                algorithmBuilder.asParseableAlgorithm(config)
        );
        var algorithm = assertInstanceOf(ListAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertEquals(List.of(ElementA.class, ElementB.class), elementClasses(algorithm.elements));
    }

    @Test
    void reconstructsAndBuildsArrayCombinations() {
        var config = combinationConfig("ArrayAlgorithm", 2);

        assertEquals(
                "ArrayAlgorithm{elements=[ElementA{},ElementB{}]}",
                algorithmBuilder.asParseableAlgorithm(config)
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

        assertEquals("ListAlgorithm{elements=[]}", algorithmBuilder.asParseableAlgorithm(config));
        var algorithm = assertInstanceOf(ListAlgorithm.class, algorithmBuilder.buildFromConfig(config));
        assertTrue(algorithm.elements.isEmpty());
    }

    @Test
    void manualDescriptionsMayRepeatAComponentClass() {
        var algorithm = assertInstanceOf(
                ListAlgorithm.class,
                algorithmBuilder.buildFromStringDescription(
                        "ListAlgorithm{elements=[ElementA{},ElementA{}]}"
                )
        );

        assertEquals(List.of(ElementA.class, ElementA.class), elementClasses(algorithm.elements));
    }

    private static AlgorithmConfiguration combinationConfig(String root, int length) {
        String collectionPath = "ROOT_" + root + ".elements";
        return new AlgorithmConfiguration(Map.of(
                "ROOT", root,
                collectionPath + ".length", Integer.toString(length),
                collectionPath + ".item0", "ElementA",
                collectionPath + ".item0_ElementA.item1", "ElementB"
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

    private static List<String> choiceNames(CombinationNode node) {
        var names = new java.util.ArrayList<String>();
        for (var choice : node.choices()) {
            names.add(choice.component().className());
        }
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
}
