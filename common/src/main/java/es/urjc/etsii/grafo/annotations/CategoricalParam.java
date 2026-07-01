package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares a categorical tunable parameter using the same categorical parameter type as irace.
 * Values are provided as strings and converted at runtime to the constructor parameter type when possible
 * (for example String, boolean, enum, numeric values, or objectives by name).
 * At least one value must be provided, except for enum parameters where omitting values uses all enum constants.
 * See section 5.1.1 Parameter Types in the official irace manual for more details.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CategoricalParam {
    /**
     * Candidate values for this parameter.
     * @return list of categorical values, or empty to use all enum constants if parameter is an enum
     */
    String[] strings() default {};
}
