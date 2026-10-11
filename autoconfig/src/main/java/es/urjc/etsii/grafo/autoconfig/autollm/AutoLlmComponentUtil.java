package es.urjc.etsii.grafo.autoconfig.autollm;

import java.util.*;

/** Naming is checked against the unfiltered component inventory. */
public final class AutoLlmComponentUtil {
    private AutoLlmComponentUtil() {}
    public static List<String> validate(Set<String> components, Set<String> baseline, int parameterCount, int maximum) {
        var added = new ArrayList<String>();
        for (String component : components) if (!baseline.contains(component)) {
            if (!component.endsWith("New")) throw new IllegalStateException("New algorithm component needs New suffix: " + component);
            added.add(component.substring(component.lastIndexOf('.') + 1));
        }
        if (parameterCount > maximum) throw new IllegalStateException("Parameter cap exceeded: " + parameterCount);
        return added;
    }
}
