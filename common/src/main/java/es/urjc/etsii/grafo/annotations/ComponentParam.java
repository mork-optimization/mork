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
     * Each disallowed class must be assignable to the annotated parameter type, or to its element type when the
     * parameter is a {@link java.util.List} or an array.
     * @return disallowed classes
     */
    Class<?>[] disallowed() default {};

    /**
     * Minimum number of elements when this annotation is used on a list or array.
     * Ignored for scalar component parameters.
     *
     * @return minimum collection size, inclusive
     */
    int min() default 0;

    /**
     * Maximum number of elements when this annotation is used on a list or array.
     * Ignored for scalar component parameters.
     *
     * @return maximum collection size, inclusive
     */
    int max() default 3;
}
