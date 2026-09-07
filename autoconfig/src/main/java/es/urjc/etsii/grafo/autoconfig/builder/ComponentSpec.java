package es.urjc.etsii.grafo.autoconfig.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A component name and the constructor parameters needed to build it.
 *
 * @param component component, alias, or factory name
 * @param parameters constructor parameters in declaration order
 */
public record ComponentSpec(String component, Map<String, Object> parameters) {

    public static final String COMPONENT_KEY = "$component";
    private static final Pattern PARAMETER_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

    public ComponentSpec {
        Objects.requireNonNull(component, "Component name cannot be null");
        if (component.isBlank()) {
            throw new IllegalArgumentException("Component name cannot be blank");
        }
        Objects.requireNonNull(parameters, "Component parameters cannot be null");

        var copy = new LinkedHashMap<String, Object>();
        for (var entry : parameters.entrySet()) {
            String name = Objects.requireNonNull(entry.getKey(), "Component parameter name cannot be null");
            validateParameterName(name);
            copy.put(name, copyValue(entry.getValue()));
        }
        parameters = Collections.unmodifiableMap(copy);
    }

    public ComponentSpec(String component) {
        this(component, Map.of());
    }

    private static Object copyValue(Object value) {
        if (value instanceof List<?> list) {
            var copy = new ArrayList<Object>(list.size());
            for (var item : list) {
                copy.add(copyValue(item));
            }
            return Collections.unmodifiableList(copy);
        }
        return value;
    }

    static void validateParameterName(String name) {
        if (name.startsWith("$")) {
            throw new IllegalArgumentException("Component parameter names starting with '$' are reserved: " + name);
        }
        if (!PARAMETER_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid component parameter name: " + name);
        }
    }
}
