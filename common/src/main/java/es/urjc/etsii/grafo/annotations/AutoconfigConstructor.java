package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * This annotation has two effects on any algorithm component:
 * - Marks that a component should be considered when automatically proposing configurations
 * - Tells the autoconfig engine which constructor to use to instantiate the component --> which other components and config parameters are mandatory
 * Note that algorithm components can still be built dynamically at runtime from their string representation even if they do not have this annotation,
 * as the correct constructor to call can be detected from the given set of parameters.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoconfigConstructor { }
