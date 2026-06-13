package es.urjc.etsii.grafo.autoconfig.fill;

import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.util.Context;

import java.util.Set;

public class ObjectiveParamProvider extends ParameterProvider {

    private static final Set<String> generateValuesForNames = Set.of("objective");
    @Override
    public boolean provides(Class<?> type, String paramName) {
        return Objective.class.isAssignableFrom(type) && generateValuesForNames.contains(paramName.toLowerCase());
    }

    @Override
    public Object getValue(Class<?> type, String paramName) {
        if (!provides(type, paramName)) {
            throw new IllegalArgumentException("Objective values generated only for Objective parameters named %s, given {type=%s, name=%s}".formatted(generateValuesForNames, type, paramName));
        }
        // TODO y el resto de objetivos?
        return Context.getMainObjective();
    }
}
