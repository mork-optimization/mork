package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;

/**
 * This annotation has two effects on any algorithm component:
 * <ul>
 *     <li>Marks that a component should be considered when automatically proposing configurations.</li>
 *     <li>Tells the autoconfig engine which constructor describes the tunable parameters and component dependencies.</li>
 * </ul>
 * Note that algorithm components can still be built dynamically at runtime from their string representation even if they do not have this annotation,
 * as the correct constructor to call can be detected from the given set of parameters.
 * A class can have at most one constructor annotated with @AutoconfigConstructor.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoconfigConstructor { }
