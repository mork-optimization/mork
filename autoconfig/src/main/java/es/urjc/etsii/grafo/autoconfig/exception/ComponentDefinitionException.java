package es.urjc.etsii.grafo.autoconfig.exception;

import java.lang.reflect.Parameter;

/** A deliberately rejected component declaration, with its owner retained for attribution. */
public final class ComponentDefinitionException extends IllegalArgumentException {
    private final Class<?> componentType;

    public ComponentDefinitionException(Class<?> componentType, String message) {
        super(message);
        this.componentType = componentType;
    }

    public ComponentDefinitionException(Parameter parameter, String message) {
        this(parameter.getDeclaringExecutable().getDeclaringClass(), message);
    }

    public Class<?> componentType() {
        return componentType;
    }
}
