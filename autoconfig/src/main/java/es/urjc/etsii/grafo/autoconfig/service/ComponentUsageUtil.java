package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/** Counts placements and direct parameter relationships in canonical component trees. */
public final class ComponentUsageUtil {
    private ComponentUsageUtil() {}

    public static ComponentUsage.Footprint analyze(JsonNode tree) {
        var components = new HashMap<String, Long>();
        var relationships = new HashMap<ComponentUsage.RelationshipKey, Long>();
        String root = componentName(tree);
        visitComponent(tree, components, relationships);
        return new ComponentUsage.Footprint(root, components, relationships);
    }

    private static String componentName(JsonNode node) {
        var name = node == null ? null : node.get(ComponentSpec.COMPONENT_KEY);
        if (name == null || !name.isString() || name.stringValue().isBlank()) {
            throw new IllegalArgumentException("Expected a canonical component object");
        }
        return name.stringValue();
    }

    private static void visitComponent(
            JsonNode node, Map<String, Long> components, Map<ComponentUsage.RelationshipKey, Long> relationships
    ) {
        String parent = componentName(node);
        components.merge(parent, 1L, Long::sum);
        for (var property : node.properties()) {
            if (!property.getKey().equals(ComponentSpec.COMPONENT_KEY)) {
                visitValue(property.getValue(), parent, property.getKey(), components, relationships);
            }
        }
    }

    private static void visitValue(
            JsonNode value, String parent, String role,
            Map<String, Long> components, Map<ComponentUsage.RelationshipKey, Long> relationships
    ) {
        if (value.isArray()) {
            for (var child : value) {
                visitValue(child, parent, role + "[]", components, relationships);
            }
        } else if (value.isObject()) {
            String child = componentName(value);
            relationships.merge(new ComponentUsage.RelationshipKey(parent, role, child), 1L, Long::sum);
            visitComponent(value, components, relationships);
        }
    }
}
