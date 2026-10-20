package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Resolves component types independently of whether any eligible implementations remain. */
public final class ComponentDependencyUtil {
    private ComponentDependencyUtil() {}

    /** Returns an empty domain for an unavailable component type, or null for an unknown type. */
    public static Collection<Class<?>> candidates(Map<Class<?>, Collection<Class<?>>> types, Class<?> type) {
        var candidates = types.get(type);
        if (candidates != null) return candidates;
        return type.isAnnotationPresent(AlgorithmComponent.class) ? List.of() : null;
    }
}
