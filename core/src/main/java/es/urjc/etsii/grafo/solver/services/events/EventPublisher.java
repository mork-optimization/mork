package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Distributes the given event to different application components.
 */
@Component
public class EventPublisher {
    private static Logger log = Logger.getLogger(EventPublisher.class.getName());
    private static ApplicationEventPublisher publisher;

    /**
     * Disable event propagation
     */
    private static boolean blockEvents = false;

    /**
     * Spring integration constructor
     * @param publisher Spring ApplicationEventPublisher
     */
    protected EventPublisher(ApplicationEventPublisher publisher) {
        EventPublisher.publisher = publisher;
    }

    /**
     * Asynchronously send and process an event
     * @param event Event to propagate
     */
    public static void publishEvent(MorkEvent event){
        if(blockEvents){
            log.fine("Event system disabled: "+event);
        }
        log.fine("Publishing event: " + event);
        publisher.publishEvent(event);
    }

    /**
     * Block dispatch of all future events. All calls to publishEvent() will be ignored.
     */
    public static void block(){
        EventPublisher.blockEvents = true;
    }

    /**
     * Enable event dispatching.
     */
    public static void unblock(){
        EventPublisher.blockEvents = false;
    }
}
