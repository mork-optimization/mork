package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * Declares how to generate values for a primitive parameter (int, double, etc) using the same strategies as Irace.
 * See section 5.1.1 Parameter Types in the official irace manual for more details
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrdinalParam {
    String[] strings() default {};
}
