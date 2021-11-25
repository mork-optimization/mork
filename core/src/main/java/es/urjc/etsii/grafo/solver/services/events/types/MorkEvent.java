package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base event. Should not be used directly
 * Users should extend this event class to include any custom data.
 * See the wiki page Event subsystem for a detailed description.
 */
public class MorkEvent extends ApplicationEvent {

    private static AtomicInteger nextEventId = new AtomicInteger(0);

    private final int eventId;
    private final String workerName;

    /**
     * Create a new {@code ApplicationEvent}.
     */
    public MorkEvent() {
        super(EventPublisher.class);
        this.eventId = nextEventId.getAndIncrement();
        this.workerName = Thread.currentThread().getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MorkEvent morkEvent = (MorkEvent) o;
        return eventId == morkEvent.eventId;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    /**
     * Get event id
     *
     * @return unique int that identifies this event
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Get event type
     *
     * @return event type or class as string
     */
    public String getType(){
        return this.getClass().getSimpleName();
    }

    /**
     * Get worker name, or in other words, who generated this event
     * @return worker name
     */
    public String getWorkerName() {
        return workerName;
    }
}
