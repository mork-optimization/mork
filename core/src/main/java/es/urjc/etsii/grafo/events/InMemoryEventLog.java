package es.urjc.etsii.grafo.events;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Append-only in-memory event replay log.
 */
@Service
public class InMemoryEventLog {

    private final List<EventEnvelope> events = new ArrayList<>();

    /**
     * Store a published event envelope.
     *
     * @param envelope event envelope
     */
    public synchronized void append(EventEnvelope envelope) {
        if (envelope.eventId() != events.size()) {
            throw new IllegalStateException("Invalid event id %s, expected %s".formatted(envelope.eventId(), events.size()));
        }
        events.add(envelope);
    }

    /**
     * Get events in range [from, to).
     *
     * @param from inclusive start index
     * @param to exclusive end index
     * @return event envelopes in the requested range
     */
    public synchronized List<EventEnvelope> getEvents(int from, int to) {
        if (from < 0) {
            throw new IllegalArgumentException("'from' must be >= 0, was " + from);
        }
        if (to < from) {
            throw new IllegalArgumentException("Invalid parameters: 'to' is less than 'from', %s < %s".formatted(to, from));
        }
        int end = Math.min(to, events.size());
        return new ArrayList<>(events.subList(from, end));
    }

    /**
     * Get a single event by id.
     *
     * @param id event id
     * @return event envelope
     */
    public synchronized EventEnvelope getEvent(int id) {
        if (id < 0 || id >= events.size()) {
            throw new IllegalArgumentException("No event with ID " + id);
        }
        return events.get(id);
    }

    /**
     * Get the last published event.
     *
     * @return latest event envelope
     */
    public synchronized EventEnvelope getLastEvent() {
        if (events.isEmpty()) {
            throw new IllegalStateException("No events have been published yet");
        }
        return events.getLast();
    }

    /**
     * Count stored events.
     *
     * @return event count
     */
    public synchronized int size() {
        return events.size();
    }
}
