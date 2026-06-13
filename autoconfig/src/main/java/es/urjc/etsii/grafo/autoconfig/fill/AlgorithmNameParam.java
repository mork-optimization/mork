package es.urjc.etsii.grafo.autoconfig.fill;

import es.urjc.etsii.grafo.util.StringUtil;

import java.util.Set;

public class AlgorithmNameParam extends ParameterProvider {

    private static final Set<String> generateValuesForNames = Set.of("algorithmName", "name", "componentName");

    @Override
    public boolean provides(Class<?> type, String paramName) {
        return type == String.class && generateValuesForNames.contains(paramName);
    }

    @Override
    public Object getValue(Class<?> type, String paramName) {
        if(!provides(type, paramName)){
            throw new IllegalArgumentException("Algorithm names generated only for String parameters named %s, given {type=%s, name=%s}".formatted(generateValuesForNames, type, paramName));
        }
        return StringUtil.randomAlgorithmName();
    }
}
