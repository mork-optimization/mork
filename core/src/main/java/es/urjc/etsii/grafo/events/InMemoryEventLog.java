package es.urjc.etsii.grafo.events;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;
import java.util.List;

/**
 * Append-only in-memory event replay log.
 */
@Service
public class InMemoryEventLog {

    private final String eventLogId = UUID.randomUUID().toString();
    private final TreeMap<Integer, EventEnvelope> events = new TreeMap<>();

    public String eventLogId() {
        return eventLogId;
    }

    public synchronized Head head() {
        return new Head(eventLogId, events.isEmpty() ? -1 : events.lastKey());
    }

    public record Head(String eventLogId, int lastEventId) {}
    public record Page(String eventLogId, int lastEventId, int from, int to, List<EventEnvelope> events) {}

    public synchronized Page page(String expectedLogId, int from, int to) {
        if (!eventLogId.equals(expectedLogId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Event log changed; fetch a new head");
        }
        if ((long) to - from > 1_000) {
            throw new IllegalArgumentException("History pages cannot exceed 1000 events");
        }
        return new Page(eventLogId, head().lastEventId(), from, to, getEvents(from, to));
    }

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
        var result = new ArrayList<EventEnvelope>();
        for (var event : events.subMap(from, true, to, false).values()) {
            result.add(event);
        }
        return List.copyOf(result);
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
