package es.urjc.etsii.grafo.autoconfig.fill;

import es.urjc.etsii.grafo.solver.Mork;

import java.util.Set;

public class FModeParam extends ParameterProvider {

    private static final Set<String> generateValuesForNames = Set.of("fmode", "mode", "ofmode");
    @Override
    public boolean provides(Class<?> type, String paramName) {
        return generateValuesForNames.contains(paramName.toLowerCase());
    }

    @Override
    public Object getValue(Class<?> type, String paramName) {
        paramName = paramName.toLowerCase();
        if (!generateValuesForNames.contains(paramName)) {
            throw new IllegalArgumentException("Fmode only for parameter names %s, given %s".formatted(generateValuesForNames, paramName));
        }
        return Mork.getFMode();
    }
}
