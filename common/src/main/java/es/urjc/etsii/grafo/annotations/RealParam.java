package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares a real-valued tunable parameter using the same real parameter type as irace.
 * Use this annotation on real-valued constructor parameters such as double, float,
 * their wrapper types, or String when the constructor expects the raw value.
 * See section 5.1.1 Parameter Types in the official irace manual for more details.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RealParam {

    /**
     * Smallest valid value in range. Range is inclusive
     * Defaults to Integer.MIN_VALUE / 2
     * @return double value
     */
    double min() default Integer.MIN_VALUE / 2;

    /**
     * Biggest valid value in range. Range is inclusive
     * Defaults to Integer.MAX_VALUE / 2
     * @return double value
     */
    double max() default Integer.MAX_VALUE / 2;

}
