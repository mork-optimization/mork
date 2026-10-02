package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.InitialConfigurationUtil;
import es.urjc.etsii.grafo.autoconfig.irace.IraceParameterFileUtil;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompactCollectionEncodingTest {
    private AutoconfigSearchSpace space;
    private AutomaticAlgorithmBuilder<TestSolution, TestInstance> builder;

    @BeforeEach void discover() {
        var roots = List.<Class<?>>of(FixedAlgorithm.class, VarargsAlgorithm.class, NestedAlgorithm.class, ZeroLengthCollectionAlgorithm.class);
        var names = new HashMap<String, Class<?>>();
        for (var type : roots) names.put(type.getSimpleName(), type);
        for (var type : List.of(StepA.class, StepB.class, StepGroup.class)) names.put(type.getSimpleName(), type);
        var inventory = new AlgorithmInventoryService.AlgorithmInventory(
                Map.of(Algorithm.class, roots, Step.class, List.of(StepA.class, StepB.class, StepGroup.class)),
                names, Map.of(), Map.of(), List.of());
        var service = mock(AlgorithmInventoryService.class);
        when(service.getInventory()).thenReturn(inventory);
        space = new AutoconfigSearchSpace(new SolverConfig(), new AlgorithmCandidateGenerator(service, new DefaultExplorationFilter()));
        builder = new AutomaticAlgorithmBuilder<>(space, new AlgorithmBuilderService(service));
    }

    @Test void fixedListsAndCappedVarargsKeepOrderAndPositionSpecificScalarValues() {
        for (var root : List.of("FixedAlgorithm", "VarargsAlgorithm")) {
            String path = "ROOT_" + root + ".steps";
            var values = new HashMap<String, String>();
            values.put("ROOT", root);
            values.put(path + ".item0", "StepB");
            values.put(path + ".item0_StepB.component.weight", "7");
            values.put(path + ".item1", "StepA");
            values.put(path + ".item1_StepA.component.weight", "3");
            var config = new AlgorithmConfiguration(values);
            var expected = new ComponentSpec(root, Map.of("steps", List.of(
                    new ComponentSpec("StepB", Map.of("weight", 7)), new ComponentSpec("StepA", Map.of("weight", 3)))));
            assertEquals(expected, builder.asComponentSpec(config));
            var algorithm = builder.buildFromConfig(config);
            List<Step> steps = algorithm instanceof FixedAlgorithm fixed ? fixed.steps : List.of(((VarargsAlgorithm) algorithm).steps);
            assertInstanceOf(StepB.class, steps.getFirst());
            assertEquals(7, steps.getFirst().weight);
            assertInstanceOf(StepA.class, steps.getLast());
            assertEquals(3, steps.getLast().weight);
            assertEquals(2, InitialConfigurationUtil.toIraceTable(space, List.of(expected)).split("\n").length);
            for (var declaration : space.iraceParameters()) assertFalse(declaration.startsWith(path + ".length\t"));
            for (var node : space.roots()) if (node.className().equals(root)) {
                assertEquals(2, node.combinations().get("steps").max());
                assertEquals(2, node.combinations().get("steps").candidates().size());
            }
            values.put(path + ".item1", "StepB");
            assertThrows(IllegalArgumentException.class, () -> builder.asComponentSpec(new AlgorithmConfiguration(values)));
        }
    }

    @Test void nestedCollectionsDecodeUnderTheirOwnPositionAndIgnoreInactiveChildren() {
        String outer = "ROOT_NestedAlgorithm.steps.item0";
        String inner = outer + "_StepGroup.component.steps";
        var config = new AlgorithmConfiguration(Map.of("ROOT", "NestedAlgorithm", outer, "StepGroup",
                inner + ".length", "2", inner + ".item0", "StepA", inner + ".item0_StepA.component.weight", "5",
                inner + ".item1", "StepB", inner + ".item1_StepB.component.weight", "9"));
        var expected = new ComponentSpec("NestedAlgorithm", Map.of("steps", List.of(new ComponentSpec("StepGroup",
                Map.of("steps", List.of(new ComponentSpec("StepA", Map.of("weight", 5)), new ComponentSpec("StepB", Map.of("weight", 9))))))));
        assertEquals(expected, builder.asComponentSpec(config));
        assertInstanceOf(NestedAlgorithm.class, builder.buildFromConfig(config));
        var empty = new AlgorithmConfiguration(Map.of("ROOT", "NestedAlgorithm", outer, "StepGroup",
                inner + ".length", "0", inner + ".item0", "NA", inner + ".item1", "NA"));
        var group = ((NestedAlgorithm) builder.buildFromConfig(empty)).steps.getFirst();
        assertTrue(((StepGroup) group).steps.isEmpty());
        assertEquals(space.iraceParameters().size(), space.snapshot().encodingDiagnostics().total().declarations().intValueExact());
        assertEquals(space.iraceParameterSpace().forbiddenExpressions().size(), space.snapshot().encodingDiagnostics().total().forbiddenConstraints().intValueExact());
    }

    @Test void zeroLengthCollectionsProduceNoSelectorsAndConstraintSectionsAreSeparate() {
        var empty = assertInstanceOf(ZeroLengthCollectionAlgorithm.class, builder.buildFromConfig(new AlgorithmConfiguration(Map.of("ROOT", "ZeroLengthCollectionAlgorithm"))));
        assertTrue(empty.steps.isEmpty());
        for (var declaration : space.iraceParameters()) assertFalse(declaration.startsWith("ROOT_ZeroLengthCollectionAlgorithm.steps"));
        String file = IraceParameterFileUtil.toFileContents(space.iraceParameterSpace());
        assertTrue(file.indexOf("[forbidden]") < file.indexOf("[global]"));
        assertEquals(3, space.iraceParameterSpace().forbiddenExpressions().size());
        assertThrows(UnsupportedOperationException.class, () -> space.iraceParameterSpace().forbiddenExpressions().clear());
        for (var root : space.roots()) assertThrows(UnsupportedOperationException.class, () -> root.combinations().get("steps").candidates().clear());
    }

    public abstract static class Step {
        final int weight;
        protected Step(int weight) { this.weight = weight; }
    }
    public static class StepA extends Step {
        @AutoconfigConstructor public StepA(@IntegerParam(min = 1, max = 100) int weight) { super(weight); }
    }
    public static class StepB extends Step {
        @AutoconfigConstructor public StepB(@IntegerParam(min = 1, max = 100) int weight) { super(weight); }
    }
    public static class StepGroup extends Step {
        final List<Step> steps;
        @AutoconfigConstructor public StepGroup(@ComponentParam(max = 2, disallowed = StepGroup.class) List<Step> steps) {
            super(0); this.steps = steps;
        }
    }
    public static class FixedAlgorithm extends Algorithm<TestSolution, TestInstance> {
        final List<Step> steps;
        @AutoconfigConstructor public FixedAlgorithm(@ComponentParam(min = 2, max = 2, disallowed = StepGroup.class) List<Step> steps) {
            super("fixed"); this.steps = steps;
        }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
    public static class VarargsAlgorithm extends Algorithm<TestSolution, TestInstance> {
        final Step[] steps;
        @AutoconfigConstructor public VarargsAlgorithm(@ComponentParam(min = 2, max = 3, disallowed = StepGroup.class) Step... steps) {
            super("varargs"); this.steps = steps;
        }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
    public static class NestedAlgorithm extends Algorithm<TestSolution, TestInstance> {
        final List<Step> steps;
        @AutoconfigConstructor public NestedAlgorithm(@ComponentParam(min = 1, max = 1, disallowed = {StepA.class, StepB.class}) List<Step> steps) {
            super("nested"); this.steps = steps;
        }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
    public static class ZeroLengthCollectionAlgorithm extends Algorithm<TestSolution, TestInstance> {
        final List<Step> steps;
        @AutoconfigConstructor public ZeroLengthCollectionAlgorithm(@ComponentParam(max = 0) List<Step> steps) {
            super("empty"); this.steps = steps;
        }
        @Override public TestSolution algorithm(TestInstance instance) { return null; }
    }
}
