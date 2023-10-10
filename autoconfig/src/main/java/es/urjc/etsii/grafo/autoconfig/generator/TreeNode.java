package es.urjc.etsii.grafo.autoconfig.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TreeNode(String paramName, Class<?> clazz, Map<String, List<TreeNode>> children) {
    public TreeNode(String paramName, Class<?> clazz) {
        this(paramName, clazz, new HashMap<>());
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
