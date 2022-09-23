package es.urjc.etsii.grafo.annotations;

import org.springframework.util.ClassUtils;

public enum ProvidedParamType {
    UNKNOWN(Object.class),
    MAXIMIZE(boolean.class),
    ALGORITHM_NAME(String.class);

    private final Class<?> providedClass;

    ProvidedParamType(Class<?> providedClass){
        this.providedClass = providedClass;
    }

    public boolean isAssignableTo(Class<?> target){
        return ClassUtils.isAssignable(target, this.providedClass);
    }
}
