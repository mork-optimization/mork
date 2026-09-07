package es.urjc.etsii.grafo.autoconfig.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TreeNode(
        String paramName,
        Class<?> clazz,
        Map<String, List<TreeNode>> children,
        Map<String, CombinationTree> combinations
) {
    public TreeNode {
        Objects.requireNonNull(paramName, "Parameter name cannot be null");
        Objects.requireNonNull(clazz, "Component class cannot be null");
        Objects.requireNonNull(children, "Children cannot be null");
        Objects.requireNonNull(combinations, "Combinations cannot be null");

        var immutableChildren = new LinkedHashMap<String, List<TreeNode>>();
        for (var entry : children.entrySet()) {
            immutableChildren.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        children = Map.copyOf(immutableChildren);
        combinations = Map.copyOf(combinations);
    }

    public TreeNode(String paramName, Class<?> clazz) {
        this(paramName, clazz, Map.of(), Map.of());
    }

    public String className(){
        return clazz.getSimpleName();
    }

    @Override
    public String toString() {
        return "Node{" +
                "n='" + paramName + '\'' +
                ", t=" + clazz +
                '}';
    }
}
