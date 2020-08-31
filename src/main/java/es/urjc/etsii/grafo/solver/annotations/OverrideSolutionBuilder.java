package es.urjc.etsii.grafo.solver.annotations;

import org.springframework.context.annotation.Primary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Primary
public @interface OverrideSolutionBuilder {
}
