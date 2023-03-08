package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Specifies additional restrictions when resolving algorithm components to available implementations
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentParam {

    /**
     * Disallowed classes for recursive components. All derived classes from the disallowed list will be disallowed too.
     * @return disallowed classes
     */
    Class<?>[] disallowed() default {};
}
