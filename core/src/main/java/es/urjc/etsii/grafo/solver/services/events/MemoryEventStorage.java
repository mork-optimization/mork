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
public class MemoryEventStorage {
    /**
     * Use a tree as events may be processed unordered, but should be retrieved ordered by range query
     */
    private final ConcurrentSkipListMap<Integer, MorkEvent> eventLog = new ConcurrentSkipListMap<>();

    protected MemoryEventStorage(){}

    /**
     * Store in memory the given event
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
     * Retrieve events by range [from, to).
     * @param from Inclusive, range start
     * @param to Not inclusive, range end
     * @return Events in range [from, to).
     */
    public List<MorkEvent> getEvents(int from, int to){
        if(to < from){
            throw new IllegalArgumentException(String.format("Invalid parameters: 'to' is less or equals to 'from', %s <= %s", to, from));
        }
        return new ArrayList<>(eventLog.subMap(from, to).values());
    }

    public Stream<? extends SolutionGeneratedEvent<?, ?>> getGeneratedSolEventForExp(String experimentName){
        return this.eventLog.values().stream()
                .filter(e -> e instanceof SolutionGeneratedEvent)
                .map(e -> (SolutionGeneratedEvent<?,?>) e)
                .filter(e -> e.getExperimentName().equals(experimentName));
    }

    @SuppressWarnings("unchecked")
    public <T extends MorkEvent> Stream<T> getEventsByType(Class<T> type){
        return (Stream<T>) this.eventLog.values().stream()
                .filter(type::isInstance);
    }

    public Stream<MorkEvent> getAllEvents(){
        return this.eventLog.values().stream();
    }

    public MorkEvent getLastEvent(){
        return eventLog.lastEntry().getValue();
    }
}
