package es.urjc.etsii.grafo.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Listen for events asynchronously
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Async
@EventListener
public @interface MorkEventListener {
}
