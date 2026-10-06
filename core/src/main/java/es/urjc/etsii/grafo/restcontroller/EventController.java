package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.PingEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


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
    public InMemoryEventLog.Page getEvents(@RequestParam String eventLogId, @RequestParam int from, @RequestParam int to) {
        try {
            return eventLog.page(eventLogId, from, to);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Get the process identity and last dispatched event ID (-1 for an empty log).
     *
     * @return event log head
     */
    @GetMapping("/lastevent")
    public InMemoryEventLog.Head getLastEvent(){
        return eventLog.head();
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
