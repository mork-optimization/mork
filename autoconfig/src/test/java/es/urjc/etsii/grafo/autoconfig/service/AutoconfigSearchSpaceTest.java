package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationChoice;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoconfigSearchSpaceTest {

    @Test
    void buildsOneImmutableCanonicalSearchSpace() {
        var solverConfig = new SolverConfig();
        solverConfig.setTreeDepth(4);
        solverConfig.setMaxDerivationRepetition(2);

        var root = new TreeNode("ROOT", TestAlgorithm.class);
        var roots = new ArrayList<>(List.of(root));
        var parameter = new ComponentParameter(
                "iterations",
                int.class,
                ParameterType.INTEGER,
                1,
                10
        );
        var parameters = new HashMap<Class<?>, List<ComponentParameter>>();
        parameters.put(TestAlgorithm.class, new ArrayList<>(List.of(parameter)));
        var iraceParameters = new ArrayList<>(List.of("ROOT parameter"));

        var generator = mock(AlgorithmCandidateGenerator.class);
        when(generator.buildTree(4, 2)).thenReturn(roots);
        when(generator.componentParams()).thenReturn(parameters);
        when(generator.toIraceParams(anyList())).thenReturn(iraceParameters);

        var searchSpace = new AutoconfigSearchSpace(solverConfig, generator);

        roots.clear();
        parameters.clear();
        iraceParameters.clear();

        assertEquals(List.of(root), searchSpace.roots());
        assertEquals(List.of(parameter), searchSpace.componentParameters().get(TestAlgorithm.class));
        assertEquals(List.of("ROOT parameter"), searchSpace.iraceParameters());
        assertEquals(1, searchSpace.snapshot().summary().generatedIraceParameterCount());
        assertThrows(UnsupportedOperationException.class, () -> searchSpace.roots().add(root));
        assertThrows(
                UnsupportedOperationException.class,
                () -> searchSpace.componentParameters().put(String.class, List.of())
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> searchSpace.componentParameters().get(TestAlgorithm.class).add(parameter)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> searchSpace.iraceParameters().add("another parameter")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> searchSpace.snapshot().roots().add("AnotherAlgorithm")
        );

        verify(generator, times(1)).buildTree(4, 2);
        verify(generator, times(1)).componentParams();
        verify(generator, times(1)).toIraceParams(searchSpace.roots());
    }

    @Test
    void searchSpaceValueObjectsDefensivelyCopyMutableInputs() {
        var values = new Object[]{1, 10};
        var parameter = new ComponentParameter("iterations", int.class, ParameterType.INTEGER, values);
        values[0] = 5;
        var returnedValues = parameter.getValues();
        returnedValues[1] = 20;
        assertArrayEquals(new Object[]{1, 10}, parameter.getValues());

        var child = new TreeNode("child", String.class);
        var childList = new ArrayList<>(List.of(child));
        var children = new HashMap<String, List<TreeNode>>();
        children.put("dependency", childList);
        var root = new TreeNode("ROOT", TestAlgorithm.class, children, Map.of());
        childList.clear();
        assertEquals(List.of(child), root.children().get("dependency"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> root.children().get("dependency").add(child)
        );

        var choices = new ArrayList<>(List.of(new CombinationChoice(child, null)));
        var combinationNode = new CombinationNode(0, choices);
        choices.clear();
        assertEquals(1, combinationNode.choices().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> combinationNode.choices().add(new CombinationChoice(child, null))
        );
    }

    private static final class TestAlgorithm {
    }
}
