package es.urjc.etsii.grafo.annotations;


import java.lang.annotation.*;

/**
 * Declares that this parameter is provided automatically by the solving engine or any extension of it.
 * This annotation should be used on parameters that are available from the configuration files, that may be constant problems.
 * Example: "boolean maximize", is not a parameter to be decided by irace, should be filled automatically.
 * The algorithm name is another thing that should be automatically generated and not decided by Irace
 * You may provide any custom parameter, see TODO
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProvidedParam {}
