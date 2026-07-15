package es.urjc.etsii.grafo.events;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Append-only in-memory event replay log.
 */
@Service
public class InMemoryEventLog {

    private final LinkedHashMap<Integer, EventEnvelope> events = new LinkedHashMap<>();

    /**
     * Store a published event envelope.
     *
     * @param envelope event envelope
     */
    public synchronized void append(EventEnvelope envelope) {
        if (!events.isEmpty() && envelope.eventId() <= events.lastEntry().getKey()) {
            throw new IllegalStateException(
                    "Event ids must be strictly increasing: %s followed %s"
                            .formatted(envelope.eventId(), events.lastEntry().getKey())
            );
        }
        events.put(envelope.eventId(), envelope);
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
        return events.values().stream()
                .filter(event -> event.eventId() >= from && event.eventId() < to)
                .toList();
    }

    /**
     * Get a single event by id.
     *
     * @param id event id
     * @return event envelope
     */
    public synchronized EventEnvelope getEvent(int id) {
        var event = events.get(id);
        if (event == null) {
            throw new IllegalArgumentException("No event with ID " + id);
        }
        return event;
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
        return events.lastEntry().getValue();
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
