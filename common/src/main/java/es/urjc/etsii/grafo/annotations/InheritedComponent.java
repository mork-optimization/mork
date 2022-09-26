package es.urjc.etsii.grafo.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Used to detect at runtime implementing classes without forcing users to use annotations.
 * Example: If abstract class SolutionSerializer is annotated with @InheritedComponent,
 * all implementations will inherit the annotation, and therefore be detected at runtime
 * as candidates for dependency injection.
 */
// TODO: review and see if its possible to swap @component with Java EE 6 Dependency Injection
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface InheritedComponent {}
