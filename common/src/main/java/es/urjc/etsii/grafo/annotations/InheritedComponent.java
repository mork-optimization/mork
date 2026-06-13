package es.urjc.etsii.grafo.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Marks an extension point whose implementations should be discovered as Spring components.
 * Example: If abstract class SolutionSerializer is annotated with @InheritedComponent,
 * all implementations will inherit the annotation, and therefore be detected at runtime
 * as candidates for dependency injection. Autoconfig extension points such as factories
 * and parameter providers use this annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Inherited
public @interface InheritedComponent {}
