package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;

import java.util.List;
import java.util.stream.Stream;

/**
 * Recover past events
 */
public abstract class AbstractEventStorage {
    /**
     * Get a list of events by id, in range [from, to)
     *
     * @param from first event id to return
     * @param to stop at this event id, without including it
     * @return List of MorkEvent
     */
    public abstract List<MorkEvent> getEvents(int from, int to);

    /**
     * Get all solution generated event for a given experiment.
     *
     * @param experimentName Experiment name
     * @return SolutionGenerated events
     */
    public abstract Stream<? extends SolutionGeneratedEvent<?, ?>> getGeneratedSolEventForExp(String experimentName);

    /**
     * Returns an event stream for the given event type, ordered by creation date.
     *
     * @param type Filter by type
     * @return Event stream
     */
    public abstract  <T extends MorkEvent> Stream<T> getEventsByType(Class<T> type);

    /**
     * Returns all MorkEvents
     *
     * @return Stream of mork event.
     */
    public abstract Stream<MorkEvent> getAllEvents();
}
