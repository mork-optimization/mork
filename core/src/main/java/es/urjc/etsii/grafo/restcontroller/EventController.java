package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import es.urjc.etsii.grafo.solver.services.events.types.PingEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Define API endpoints related to events.
 */
@RestController
public class EventController {

    private final MemoryEventStorage memoryEventStorage;

    /**
     * Create controller, done by Spring
     *
     * @param memoryEventStorage event storage
     */
    public EventController(MemoryEventStorage memoryEventStorage) {
        this.memoryEventStorage = memoryEventStorage;
    }

    /**
     * Get events in range [from, to)
     *
     * @param from Inclusive, range start
     * @param to Not inclusive, range end
     * @return Events in range [from, to).
     */
    @GetMapping("/events")
    public List<MorkEvent> getEvents(@RequestParam int from, @RequestParam int to) {
        return memoryEventStorage.getEvents(from, to);
    }

    /**
     * Get latest generated event
     *
     * @return last event
     */
    @GetMapping("/lastevent")
    public MorkEvent getLastEvent(){
        return memoryEventStorage.getLastEvent();
    }

    /**
     * Force the creation of an event, useful for debugging purposes
     *
     * @return created event
     */
    @GetMapping("/ping")
    public PingEvent ping(){
        var ping = new PingEvent();
        EventPublisher.getInstance().publishEvent(ping);
        return ping;
    }
}
