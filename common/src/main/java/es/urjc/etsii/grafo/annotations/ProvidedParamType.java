package es.urjc.etsii.grafo.annotations;

import es.urjc.etsii.grafo.algorithms.FMode;
import org.springframework.util.ClassUtils;

public enum ProvidedParamType {
    UNKNOWN(Object.class),
    MAXIMIZE(FMode.class),
    ALGORITHM_NAME(String.class);

    private final Class<?> providedClass;

    ProvidedParamType(Class<?> providedClass){
        this.providedClass = providedClass;
    }

    public boolean isAssignableTo(Class<?> target){
        return ClassUtils.isAssignable(target, this.providedClass);
    }
}
