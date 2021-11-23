package es.urjc.etsii.grafo.solver.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Used to detect at runtime implementing classes without forcing users to use annotations
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface InheritedComponent {}
