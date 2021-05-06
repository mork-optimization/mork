package es.urjc.etsii.grafo.solver.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
/**
 * Used to detect at runtime implementing classes without forcing users to use annotations
 */
public @interface InheritedComponent {}