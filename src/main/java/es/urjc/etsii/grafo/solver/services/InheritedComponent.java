package es.urjc.etsii.grafo.solver.services;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface InheritedComponent {}