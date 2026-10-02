package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoconfigEncodingUtilTest {
    @Test
    void estimatesFactorialPrefixesWithoutConstructingThemOrOverflowingLongs() {
        var candidates = new ArrayList<TreeNode>();
        var metadata = new HashMap<Class<?>, List<ComponentParameter>>();
        var classes = new ArrayList<Class<?>>();
        // Distinct array classes supply a large synthetic candidate set without 100 fixture classes.
        for (int dimensions = 1; dimensions <= 100; dimensions++) {
            var type = Array.newInstance(Object.class, new int[dimensions]).getClass();
            classes.add(type);
            candidates.add(new TreeNode("phases", type));
            metadata.put(type, List.of());
        }
        var combination = new CombinationTree(100, 100, candidates);
        var root = new TreeNode("ROOT", Root.class, Map.of(), Map.of("phases", combination));
        metadata.put(Root.class, List.of(ComponentParameter.combination("phases", List.class, Object.class, classes, 100, 100)));
        var counts = AutoconfigEncodingUtil.count(List.of(root), metadata);
        assertEquals(BigInteger.valueOf(101), counts.declarations());
        assertEquals(BigInteger.valueOf(4950), counts.forbiddenConstraints());
        assertTrue(counts.prefixDeclarationEstimate().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
    }

    @Test
    void handlesEmptyCollectionsAndProvidedParametersWithoutPhantomSelectors() {
        var root = new TreeNode("ROOT", Root.class, Map.of(), Map.of("phases", new CombinationTree(0, 0, List.of())));
        Map<Class<?>, List<ComponentParameter>> metadata = Map.of(Root.class, List.of(
                ComponentParameter.combination("phases", List.class, Object.class, List.of(), 0, 0),
                new ComponentParameter("objective", Object.class, ParameterType.PROVIDED, new Object[0])));
        var counts = AutoconfigEncodingUtil.count(List.of(root), metadata);
        assertEquals(BigInteger.ONE, counts.declarations());
        assertEquals(BigInteger.ONE, counts.prefixDeclarationEstimate());
        assertEquals(BigInteger.ZERO, counts.forbiddenConstraints());
    }

    private static final class Root {}
}
