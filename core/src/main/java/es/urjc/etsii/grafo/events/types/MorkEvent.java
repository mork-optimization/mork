package es.urjc.etsii.grafo.events.types;

/**
 * Base event payload. Should not be used directly.
 * Users should extend this event class to include any custom data.
 */
public class MorkEvent {
    private final String workerName;

    /**
     * Create a new event payload.
     */
    public MorkEvent() {
        this.workerName = Thread.currentThread().getName();
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
