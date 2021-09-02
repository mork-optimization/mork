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

    protected DefaultEventListener(SimpMessagingTemplate simpMessagingTemplate, MemoryEventStorage memoryEventStorage) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.memoryEventStorage = memoryEventStorage;
    }

    @MorkEventListener
    public void sendToWebsocket(MorkEvent applicationEvent){
        log.fine(String.format("Sending event to websocket path %s: %s", eventPath, applicationEvent));
        memoryEventStorage.storeEvent(applicationEvent);
        simpMessagingTemplate.convertAndSend(eventPath, applicationEvent);
    }
}
