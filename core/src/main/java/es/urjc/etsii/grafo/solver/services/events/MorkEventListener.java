package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.*;


/**
 * Listen for events asynchronously
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Async
@EventListener
public @interface MorkEventListener {
}
