package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.logging.Logger;

/**
 * Default Event listener responsible for sending framework events
 * via websockets and storing a copy in an EventStorage
 */
public class DefaultEventListener extends AbstractEventListener {
    private static Logger log = Logger.getLogger(DefaultEventListener.class.getName());

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final String eventPath = "/topic/events";

    private final MemoryEventStorage memoryEventStorage;

    /**
     * Create DefaultEventListener
     *
     * @param simpMessagingTemplate websocket messaging template
     * @param memoryEventStorage memory event storage
     */
    protected DefaultEventListener(SimpMessagingTemplate simpMessagingTemplate, MemoryEventStorage memoryEventStorage) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.memoryEventStorage = memoryEventStorage;
    }

    /**
     * Store event in memory and send to websocket
     *
     * @param morkEvent Mork event
     */
    @MorkEventListener
    public void processEvent(MorkEvent morkEvent){
        log.fine(String.format("Sending event to websocket path %s: %s", eventPath, morkEvent));
        memoryEventStorage.storeEvent(morkEvent);
        simpMessagingTemplate.convertAndSend(eventPath, morkEvent);
    }
}
