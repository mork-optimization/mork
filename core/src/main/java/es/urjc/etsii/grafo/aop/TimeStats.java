package es.urjc.etsii.grafo.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TimeStats {

//    LogLevel value() default LogLevel.INFO;
//
//    ChronoUnit unit() default ChronoUnit.SECONDS;
//    boolean showArgs() default false;
//    boolean showResult() default false;
//    boolean showExecutionTime() default true;
}
