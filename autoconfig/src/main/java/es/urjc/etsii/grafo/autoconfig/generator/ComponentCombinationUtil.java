package es.urjc.etsii.grafo.autoconfig.generator;

import java.util.Set;

/** Shared validation for automatic collection reconstruction and initial configurations. */
public final class ComponentCombinationUtil {
    private ComponentCombinationUtil() {}

    public static void validateLength(CombinationTree combination, int length, String path) {
        if (length < combination.min() || length > combination.max()) {
            throw new IllegalArgumentException("Invalid length %s for %s, expected range [%s, %s]"
                    .formatted(length, path, combination.min(), combination.max()));
        }
    }

    public static TreeNode select(CombinationTree combination, String name, String path, Set<Class<?>> used) {
        for (var candidate : combination.candidates()) {
            if (candidate.className().equals(name)) {
                if (!used.add(candidate.clazz())) {
                    throw new IllegalArgumentException("Repeated component %s at %s".formatted(name, path));
                }
                return candidate;
            }
        }
        throw new IllegalArgumentException("Component %s is not allowed at %s".formatted(name, path));
    }
}
