package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares an ordinal tunable parameter using the same ordinal parameter type as irace.
 * Values are provided as strings and converted at runtime to the constructor parameter type when possible.
 * At least one value must be provided, ordered from lowest to highest according to the domain.
 * See section 5.1.1 Parameter Types in the official irace manual for more details.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrdinalParam {
    /**
     * Ordered candidate values for this parameter.
     * @return non-empty list of ordinal values
     */
    String[] strings() default {};
}
