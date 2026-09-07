package es.urjc.etsii.grafo.autoconfig.builder;

import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Converts flattened JSON algorithm descriptions to and from {@link ComponentSpec}.
 */
public final class ComponentSpecJsonCodec {

    private final JsonMapper mapper;

    public ComponentSpecJsonCodec() {
        this.mapper = JsonMapper.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
    }

    public ComponentSpec parse(String json) {
        if (json == null) {
            throw new AlgorithmParsingException("Algorithm JSON description cannot be null");
        }
        try {
            JsonNode root = mapper.readTree(json);
            if (root == null) {
                throw new AlgorithmParsingException("Algorithm JSON description cannot be empty");
            }
            return parseComponent(root, "$");
        } catch (JacksonException e) {
            var location = e.getLocation();
            String locationText = location == null
                    ? ""
                    : " at line %s, column %s".formatted(location.getLineNr(), location.getColumnNr());
            throw new AlgorithmParsingException("Invalid algorithm JSON%s: %s"
                    .formatted(locationText, e.getOriginalMessage()), e);
        }
    }

    public String toJson(ComponentSpec spec) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toJsonTree(spec));
        } catch (JacksonException e) {
            throw new AlgorithmParsingException("Failed to serialize algorithm description as JSON", e);
        }
    }

    public JsonNode toJsonTree(ComponentSpec spec) {
        return componentToJson(spec, "$");
    }

    private ComponentSpec parseComponent(JsonNode node, String path) {
        if (!node.isObject()) {
            throw invalid(path, "expected a component object");
        }

        JsonNode componentNode = node.get(ComponentSpec.COMPONENT_KEY);
        if (componentNode == null) {
            throw invalid(path, "missing required property '" + ComponentSpec.COMPONENT_KEY + "'");
        }
        if (!componentNode.isString()) {
            throw invalid(path + "/" + ComponentSpec.COMPONENT_KEY, "expected a string");
        }
        String component = componentNode.stringValue();
        if (component.isBlank()) {
            throw invalid(path + "/" + ComponentSpec.COMPONENT_KEY, "component name cannot be blank");
        }

        var parameters = new LinkedHashMap<String, Object>();
        for (var property : node.properties()) {
            String name = property.getKey();
            if (name.equals(ComponentSpec.COMPONENT_KEY)) {
                continue;
            }
            String propertyPath = path + "/" + escapeJsonPointer(name);
            if (name.startsWith("$")) {
                throw invalid(propertyPath, "unknown reserved property");
            }
            try {
                ComponentSpec.validateParameterName(name);
            } catch (IllegalArgumentException e) {
                throw invalid(propertyPath, e.getMessage());
            }
            parameters.put(name, parseValue(property.getValue(), propertyPath));
        }

        try {
            return new ComponentSpec(component, parameters);
        } catch (IllegalArgumentException e) {
            throw invalid(path, e.getMessage());
        }
    }

    private Object parseValue(JsonNode node, String path) {
        if (node.isObject()) {
            return parseComponent(node, path);
        }
        if (node.isArray()) {
            var values = new ArrayList<Object>(node.size());
            int index = 0;
            for (var child : node) {
                values.add(parseValue(child, path + "/" + index));
                index++;
            }
            return Collections.unmodifiableList(values);
        }
        if (node.isNull()) {
            return null;
        }
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber()) {
            if (node.canConvertToInt()) {
                return node.intValue();
            }
            if (node.canConvertToLong()) {
                return node.longValue();
            }
            throw invalid(path, "integer value is outside the signed 64-bit range");
        }
        if (node.isFloatingPointNumber()) {
            double value = node.doubleValue();
            if (!Double.isFinite(value)) {
                throw invalid(path, "floating-point value must be finite");
            }
            return value;
        }
        throw invalid(path, "unsupported JSON value");
    }

    private JsonNode componentToJson(ComponentSpec spec, String path) {
        if (spec == null) {
            throw invalid(path, "component specification cannot be null");
        }
        var object = mapper.createObjectNode();
        object.put(ComponentSpec.COMPONENT_KEY, spec.component());
        for (var parameter : spec.parameters().entrySet()) {
            String parameterPath = path + "/" + escapeJsonPointer(parameter.getKey());
            object.set(parameter.getKey(), valueToJson(parameter.getValue(), parameterPath));
        }
        return object;
    }

    private JsonNode valueToJson(Object value, String path) {
        JsonNodeFactory nodes = mapper.getNodeFactory();
        switch (value) {
            case null -> {
                return nodes.nullNode();
            }
            case ComponentSpec spec -> {
                return componentToJson(spec, path);
            }
            case List<?> list -> {
                var array = mapper.createArrayNode();
                for (int i = 0; i < list.size(); i++) {
                    array.add(valueToJson(list.get(i), path + "/" + i));
                }
                return array;
            }
            case String string -> {
                return nodes.stringNode(string);
            }
            case Character character -> {
                return nodes.stringNode(character.toString());
            }
            case Boolean bool -> {
                return nodes.booleanNode(bool);
            }
            case Byte number -> {
                return nodes.numberNode(number);
            }
            case Short number -> {
                return nodes.numberNode(number);
            }
            case Integer number -> {
                return nodes.numberNode(number);
            }
            case Long number -> {
                return nodes.numberNode(number);
            }
            case Float number -> {
                if (!Float.isFinite(number)) {
                    throw invalid(path, "floating-point value must be finite");
                }
                return nodes.numberNode(number);
            }
            case Double number -> {
                if (!Double.isFinite(number)) {
                    throw invalid(path, "floating-point value must be finite");
                }
                return nodes.numberNode(number);
            }
            default -> throw invalid(path, "unsupported component value type " + value.getClass().getName());
        }
    }

    private static AlgorithmParsingException invalid(String path, String message) {
        return new AlgorithmParsingException("Invalid algorithm JSON at " + path + ": " + message);
    }

    private static String escapeJsonPointer(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }
}
