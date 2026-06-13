package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares an integer tunable parameter using the same integer parameter type as irace.
 * Use this annotation on integer-compatible constructor parameters such as int, long, short, byte,
 * their wrapper types, or String when the constructor expects the raw value.
 * See section 5.1.1 Parameter Types in the official irace manual for more details.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IntegerParam {

    /**
     * Smallest valid value in range. Range is inclusive
     * Defaults to Integer.MIN_VALUE / 2
     * @return int value
     */
    int min() default Integer.MIN_VALUE / 2;

    /**
     * Biggest valid value in range. Range is inclusive
     * Defaults to Integer.MAX_VALUE / 2
     * @return int value
     */
    int max() default Integer.MAX_VALUE / 2;
}
