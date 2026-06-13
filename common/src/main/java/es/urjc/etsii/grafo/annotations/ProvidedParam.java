package es.urjc.etsii.grafo.annotations;


import java.lang.annotation.*;

/**
 * Declares that this parameter is provided automatically by the solving engine or any extension of it.
 * This annotation should be used on constructor parameters that should not be decided by irace,
 * such as generated algorithm names or framework-level objectives.
 * Values are resolved at runtime by exactly one matching autoconfig ParameterProvider.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProvidedParam {}
