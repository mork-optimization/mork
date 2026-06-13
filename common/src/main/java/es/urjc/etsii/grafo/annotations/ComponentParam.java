package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Specifies additional restrictions when resolving a constructor parameter whose type is another algorithm component.
 * Use this annotation when the default recursive resolution must exclude one or more implementations.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentParam {

    /**
     * Disallowed classes for recursive components. All derived classes from the disallowed list will be disallowed too.
     * Each disallowed class must be assignable to the annotated parameter type.
     * @return disallowed classes
     */
    Class<?>[] disallowed() default {};
}
