package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationTree;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InitialConfigurationUtilTest {

    @Test
    void writesValidRowsForConditionalComponentChoices() {
        var space = searchSpace();
        var first = root(4, "FIRST", constructiveA(0.5), List.of(stepA(3)));
        var second = root(8, "BEST", new ComponentSpec(ConstructiveB.class.getSimpleName()),
                List.of(new ComponentSpec(StepB.class.getSimpleName()), stepA(5)));

        String[] lines = InitialConfigurationUtil.toIraceTable(space, List.of(first, second)).split("\n");
        String[] names = lines[0].split("\t");
        String[] firstRow = lines[1].split("\t");
        String[] secondRow = lines[2].split("\t");

        assertEquals(names.length, firstRow.length);
        assertEquals(names.length, secondRow.length);
        assertEquals("\"ExampleAlgorithm\"", firstRow[index(names, "ROOT")]);
        assertEquals("\"4\"", firstRow[index(names, "ROOT_ExampleAlgorithm.iterations")]);
        assertEquals("\"'\\\"FIRST\\\"'\"", firstRow[index(names, "ROOT_ExampleAlgorithm.selection")]);
        assertEquals("\"ConstructiveA\"", firstRow[index(names, "ROOT_ExampleAlgorithm.constructive")]);
        assertEquals("\"0.50\"", firstRow[index(names, "ROOT_ExampleAlgorithm.constructive_ConstructiveA.bias")]);
        assertEquals("NA", secondRow[index(names, "ROOT_ExampleAlgorithm.constructive_ConstructiveA.bias")]);
        assertEquals("\"1\"", firstRow[index(names, "ROOT_ExampleAlgorithm.steps.length")]);
        assertEquals("\"StepA\"", firstRow[index(names, "ROOT_ExampleAlgorithm.steps.item0")]);
        assertEquals("\"3\"", firstRow[index(names, "ROOT_ExampleAlgorithm.steps.item0_StepA.component.weight")]);
        assertEquals("NA", firstRow[index(names, "ROOT_ExampleAlgorithm.steps.item1")]);
        assertEquals("\"StepA\"", secondRow[index(names, "ROOT_ExampleAlgorithm.steps.item1")]);
        assertEquals("\"5\"", secondRow[index(names, "ROOT_ExampleAlgorithm.steps.item1_StepA.component.weight")]);
        assertEquals("NA", secondRow[index(names, "ROOT_ExampleAlgorithm.steps.item0_StepA.component.weight")]);
    }

    @Test
    void rejectsSeedsOutsideTheGeneratedSpace() {
        var space = searchSpace();
        var invalidChoice = root(4, "FIRST", constructiveA(0.5), List.of(stepA(3), stepA(5)));
        var error = assertThrows(IllegalArgumentException.class,
                () -> InitialConfigurationUtil.toIraceTable(space, List.of(invalidChoice)));
        assertTrue(error.getMessage().contains("Repeated component"));

        var missingParameter = new ComponentSpec(ExampleAlgorithm.class.getSimpleName(), Map.of("iterations", 4));
        error = assertThrows(IllegalArgumentException.class,
                () -> InitialConfigurationUtil.toIraceTable(space, List.of(missingParameter)));
        assertTrue(error.getMessage().contains("Missing initial configuration parameter"));

        var valid = root(4, "FIRST", constructiveA(0.5), List.of(stepA(3)));
        error = assertThrows(IllegalArgumentException.class,
                () -> InitialConfigurationUtil.toIraceTable(space, List.of(valid, valid)));
        assertTrue(error.getMessage().contains("Duplicate"));

        var excessivePrecision = root(4, "FIRST", constructiveA(0.501), List.of(stepA(3)));
        error = assertThrows(IllegalArgumentException.class,
                () -> InitialConfigurationUtil.toIraceTable(space, List.of(excessivePrecision)));
        assertTrue(error.getMessage().contains("decimal places"));
    }

    private static AutoconfigSearchSpace searchSpace() {
        var stepA = new TreeNode("steps", StepA.class);
        var stepB = new TreeNode("steps", StepB.class);
        var combination = new CombinationTree(1, 2, List.of(stepA, stepB));
        var constructiveA = new TreeNode("constructive", ConstructiveA.class);
        var constructiveB = new TreeNode("constructive", ConstructiveB.class);
        var root = new TreeNode("ROOT", ExampleAlgorithm.class,
                Map.of("constructive", List.of(constructiveA, constructiveB)), Map.of("steps", combination));
        Map<Class<?>, List<ComponentParameter>> params = Map.of(
                ExampleAlgorithm.class, List.of(
                        new ComponentParameter("algorithmName", String.class, ParameterType.PROVIDED, new Object[0]),
                        new ComponentParameter("iterations", int.class, ParameterType.INTEGER, 1, 10),
                        new ComponentParameter("selection", String.class, ParameterType.CATEGORICAL,
                                new Object[]{"FIRST", "BEST"}),
                        new ComponentParameter("constructive", Object.class, ParameterType.NOT_ANNOTATED,
                                new Object[]{ConstructiveA.class, ConstructiveB.class}),
                        ComponentParameter.combination("steps", List.class, Object.class,
                                List.of(StepA.class, StepB.class), 1, 2)),
                ConstructiveA.class, List.of(new ComponentParameter("bias", double.class, ParameterType.REAL, 0.0, 1.0)),
                ConstructiveB.class, List.<ComponentParameter>of(),
                StepA.class, List.of(new ComponentParameter("weight", int.class, ParameterType.INTEGER, 1, 5)),
                StepB.class, List.<ComponentParameter>of());
        var names = List.of(
                "ROOT",
                "ROOT_ExampleAlgorithm.iterations",
                "ROOT_ExampleAlgorithm.selection",
                "ROOT_ExampleAlgorithm.constructive",
                "ROOT_ExampleAlgorithm.constructive_ConstructiveA.bias",
                "ROOT_ExampleAlgorithm.steps.length",
                "ROOT_ExampleAlgorithm.steps.item0",
                "ROOT_ExampleAlgorithm.steps.item0_StepA.component.weight",
                "ROOT_ExampleAlgorithm.steps.item1",
                "ROOT_ExampleAlgorithm.steps.item1_StepA.component.weight");
        var generator = mock(AlgorithmCandidateGenerator.class);
        when(generator.buildTree(4, 2)).thenReturn(List.of(root));
        when(generator.componentParams()).thenReturn(params);
        when(generator.toIraceParameterSpace(anyList())).thenReturn(new IraceParameterSpace(names, List.of()));
        return new AutoconfigSearchSpace(new SolverConfig(), generator);
    }

    private static ComponentSpec root(int iterations, String selection, ComponentSpec constructive,
                                      List<ComponentSpec> steps) {
        var params = new LinkedHashMap<String, Object>();
        params.put("iterations", iterations);
        params.put("selection", selection);
        params.put("constructive", constructive);
        params.put("steps", steps);
        return new ComponentSpec(ExampleAlgorithm.class.getSimpleName(), params);
    }

    private static ComponentSpec constructiveA(double bias) {
        return new ComponentSpec(ConstructiveA.class.getSimpleName(), Map.of("bias", bias));
    }

    private static ComponentSpec stepA(int weight) {
        return new ComponentSpec(StepA.class.getSimpleName(), Map.of("weight", weight));
    }

    private static int index(String[] names, String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Missing column " + name);
    }

    private static final class ExampleAlgorithm {
    }

    private static final class StepA {
    }

    private static final class StepB {
    }

    private static final class ConstructiveA {
    }

    private static final class ConstructiveB {
    }
}
