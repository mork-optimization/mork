package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;

import java.util.List;
import java.util.Optional;
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
     * Get a single event with the given id
     * @param id event id
     * @return event with the given id
     * @throws IllegalArgumentException if the id is not valid
     */
    public abstract MorkEvent getEvent(int id);

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
     * @param <T> Event type
     * @return Event stream
     */
    public abstract <T extends MorkEvent> Stream<T> getEventsByType(Class<T> type);

    /**
     * Returns all MorkEvents
     *
     * @return Stream of mork event.
     */
    public abstract Stream<MorkEvent> getAllEvents();

    /**
     * Count solutions in memory (not garbage collected) for a given experiment
     * @return solution in memory for the given experiment
     */
    public long solutionsInMemory(String experimentName){
        return this.getGeneratedSolEventForExp(experimentName)
                .map(SolutionGeneratedEvent::getSolution)
                .filter(Optional::isPresent)
                .count();
    }
}
