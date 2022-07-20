package es.urjc.etsii.grafo.annotations;

import java.lang.annotation.*;


/**
 * Annotation to mark any given class as an algorithm component.
 * It is propagated to subclasses, for example if LocalSearch is annotated with it
 * both BestImprovementLocalSearch and FirstImprovementLocalSearch inherit the annotation.
 * Internally it is used to autodetect all components that can be used to build algorithms at runtime.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AlgorithmComponent {}
