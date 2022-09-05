package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares how to generate values for a primitive parameter (int, double, etc.) using the same strategies as Irace.
 * See section 5.1.1 Parameter Types in the official irace manual for more details
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RealParam {

    /**
     * Smallest valid value in range. Range is inclusive
     * Defaults to Double.MIN_VALUE / 2
     * @return double value
     */
    double min() default Double.MIN_VALUE / 2;

    /**
     * Biggest valid value in range. Range is inclusive
     * Defaults to Double.MAX_VALUE / 2
     * @return double value
     */
    double max() default Double.MAX_VALUE / 2;

}
