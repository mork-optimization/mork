package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Builds small representative configurations from the actual bounded derivation tree. */
public final class AutoLlmProbeUtil {
    private AutoLlmProbeUtil() {}
    public static ComponentSpec sample(AutoconfigSearchSpace space, String target) {
        for (var root : space.roots()) if (contains(root, target)) return sampleNode(space, root, target);
        throw new IllegalArgumentException("Component is unreachable: " + target);
    }
    private static boolean contains(TreeNode node, String name) {
        if (node.className().equals(name)) return true;
        for (var children : node.children().values()) for (var child : children) if (contains(child, name)) return true;
        for (var group : node.combinations().values()) for (var child : group.candidates()) if (contains(child, name)) return true;
        return false;
    }
    private static ComponentSpec sampleNode(AutoconfigSearchSpace space, TreeNode node, String target) {
        var values = new LinkedHashMap<String, Object>();
        for (var p : space.componentParameters().get(node.clazz())) {
            if (p.getType() == ParameterType.PROVIDED) continue;
            if (p.combination()) {
                var group = node.combinations().get(p.getName());
                var choices = new ArrayList<>(group.candidates());
                choices.sort(Comparator.comparing(n -> !contains(n, target)));
                int count = group.min();
                if (count == 0 && group.max() > 0 && !choices.isEmpty() && contains(choices.getFirst(), target)) count = 1;
                var items = new ArrayList<ComponentSpec>();
                for (int i = 0; i < count; i++) items.add(sampleNode(space, choices.get(i), target));
                values.put(p.getName(), items);
            } else if (p.recursive()) {
                var children = node.children().get(p.getName());
                TreeNode selected = children.getFirst();
                for (var child : children) if (contains(child, target)) { selected = child; break; }
                values.put(p.getName(), sampleNode(space, selected, target));
            } else if (p.getType() == ParameterType.INTEGER) {
                values.put(p.getName(), ((Number) p.getValues()[0]).intValue());
            } else if (p.getType() == ParameterType.REAL) {
                var low = new BigDecimal(p.getValues()[0].toString()).setScale(2, RoundingMode.CEILING);
                values.put(p.getName(), low.doubleValue());
            } else values.put(p.getName(), p.getValues()[0]);
        }
        return new ComponentSpec(node.className(), values);
    }
}
