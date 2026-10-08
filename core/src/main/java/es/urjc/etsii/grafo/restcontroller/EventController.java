package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.events.EventEnvelope;
import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.PingEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Define API endpoints related to events.
 */
@RestController
@CrossOrigin
public class EventController {

    private final InMemoryEventLog eventLog;
    private final MorkEventPublisher eventPublisher;

    /**
     * Create controller, done by Spring
     *
     * @param eventLog event replay log
     * @param eventPublisher event publisher
     */
    public EventController(InMemoryEventLog eventLog, MorkEventPublisher eventPublisher) {
        this.eventLog = eventLog;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get events in range [from, to)
     *
     * @param from Inclusive, range start
     * @param to Not inclusive, range end
     * @return Events in range [from, to).
     */
    @GetMapping("/events")
    public List<EventEnvelope> getEvents(@RequestParam int from, @RequestParam int to) {
        return eventLog.getEvents(from, to);
    }

    /**
     * Get latest generated event
     *
     * @return last event
     */
    @GetMapping("/lastevent")
    public EventEnvelope getLastEvent(){
        return eventLog.getLastEvent();
    }

    /**
     * Force the creation of an event, useful for debugging purposes
     *
     * @return created event
     */
    @GetMapping("/ping")
    public PingEvent ping(){
        var ping = new PingEvent();
        eventPublisher.publish(ping);
        return ping;
    }
}
