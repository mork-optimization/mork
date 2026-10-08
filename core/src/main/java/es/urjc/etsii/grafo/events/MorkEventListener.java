package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;

/**
 * Listen for Mork events.
 *
 * Events are already dispatched from the Mork event dispatcher thread, outside the
 * solver hot path. Listeners therefore execute synchronously in event order unless
 * they explicitly delegate work elsewhere. Implementations under Mork's configured
 * scan packages are discovered automatically and do not need a Spring stereotype.
 */
@FunctionalInterface
public interface MorkEventListener {

    /**
     * Handle a Mork event. Implementations may use pattern matching to select
     * the event types they consume.
     *
     * @param event dispatched event
     */
    void onEvent(MorkEvent event);
}
