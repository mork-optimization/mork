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

    protected final int eventId;
    protected final String workerName;

    /**
     * Create a new {@code ApplicationEvent}.
     */
    public MorkEvent() {
        super(EventPublisher.class);
        this.eventId = nextEventId.getAndIncrement();
        this.workerName = Thread.currentThread().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MorkEvent morkEvent = (MorkEvent) o;
        return eventId == morkEvent.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    public int getEventId() {
        return eventId;
    }

    public String getType(){
        return this.getClass().getSimpleName();
    }

    public String getWorkerName() {
        return workerName;
    }
}
