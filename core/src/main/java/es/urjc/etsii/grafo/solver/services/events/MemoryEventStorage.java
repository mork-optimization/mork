package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

/**
 * Store historical event data
 */
@Service
public class MemoryEventStorage extends AbstractEventStorage {
    /**
     * Use a tree as events may be processed unordered, but should be retrieved ordered by range query
     */
    private final ConcurrentSkipListMap<Integer, MorkEvent> eventLog = new ConcurrentSkipListMap<>();

    /**
     * <p>Constructor for MemoryEventStorage.</p>
     */
    protected MemoryEventStorage(){}

    /**
     * Store in memory the given event
     *
     * @param event event to save
     */
    public void storeEvent(MorkEvent event){
        int eventId = event.getEventId();
        if(eventLog.containsKey(eventId)){
            throw new IllegalStateException("Repeated event id + " + eventId);
        }
        eventLog.put(eventId, event);
    }

    /**
     * {@inheritDoc}
     *
     * Retrieve events by range [from, to).
     */
    public List<MorkEvent> getEvents(int from, int to){
        if(to < from){
            throw new IllegalArgumentException(String.format("Invalid parameters: 'to' is less or equals to 'from', %s <= %s", to, from));
        }
        return new ArrayList<>(eventLog.subMap(from, to).values());
    }

    @Override
    public MorkEvent getEvent(int id) {
        var event = eventLog.get(id);
        if(event == null){
            throw new IllegalArgumentException("No event with ID " + id);
        }
        return event;
    }

    /** {@inheritDoc} */
    public Stream<? extends SolutionGeneratedEvent<?, ?>> getGeneratedSolEventForExp(String experimentName){
        return this.eventLog.values().stream()
                .filter(e -> e instanceof SolutionGeneratedEvent)
                .map(e -> (SolutionGeneratedEvent<?,?>) e)
                .filter(e -> e.getExperimentName().equals(experimentName));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T extends MorkEvent> Stream<T> getEventsByType(Class<T> type){
        return (Stream<T>) this.eventLog.values().stream()
                .filter(type::isInstance);
    }

    /**
     * <p>getAllEvents.</p>
     *
     * @return a {@link java.util.stream.Stream} object.
     */
    public Stream<MorkEvent> getAllEvents(){
        return this.eventLog.values().stream();
    }

    /**
     * <p>getLastEvent.</p>
     *
     * @return a {@link es.urjc.etsii.grafo.solver.services.events.types.MorkEvent} object.
     */
    public MorkEvent getLastEvent(){
        return eventLog.lastEntry().getValue();
    }
}
