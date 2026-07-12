package es.urjc.etsii.grafo.events;

import org.springframework.context.event.EventListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Listen for Mork events.
 *
 * Events are already dispatched from the Mork event dispatcher thread, outside the
 * solver hot path. Listener methods therefore execute synchronously in event
 * order unless they explicitly delegate work elsewhere.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventListener
public @interface MorkEventListener {
}
