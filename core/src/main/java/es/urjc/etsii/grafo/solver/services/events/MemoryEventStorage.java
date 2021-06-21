package es.urjc.etsii.grafo.solver.services.events;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Store historical event data
 */
@Service
public class MemoryEventStorage {
    /**
     * Use a tree as events may be processed unordered, but should be retrieved ordered by range query
     */
    private TreeMap<Integer, MorkEvent> eventLog = new TreeMap();

    protected MemoryEventStorage(){}

    /**
     * Store in memory the given event
     * @param event event to save
     */
    public void storeEvent(MorkEvent event){
        // TODO Storing all generated solutions via events
        // can get really memory expensive. Free them?
        // Use this mechanism instead of lists of WorkingOnResults, etc?
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
        if(to <= from){
            throw new IllegalArgumentException(String.format("Invalid parameters: 'to' is less or equals to 'from', %s <= %s", to, from));
        }
        return new ArrayList<>(eventLog.subMap(from, to).values());
    }
}
