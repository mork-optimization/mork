package es.urjc.etsii.grafo.autoconfig.fill;

import es.urjc.etsii.grafo.util.Context;

import java.util.Set;

public class ObjectiveParamProvider extends ParameterProvider {

    private static final Set<String> generateValuesForNames = Set.of("objective");
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
        // TODO y el resto de objetivos?
        return Context.getMainObjective();
    }
}
