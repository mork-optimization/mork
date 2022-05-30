package es.urjc.etsii.grafo.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Used to detect at runtime implementing classes without forcing users to use annotations
 */
// TODO: change @component with Dependency Injection (CDI) of Java EE 6
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface InheritedComponent {}
