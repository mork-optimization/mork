package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;

/**
 * Public representation of a published event.
 *
 * @param eventId dense event id assigned during ordered dispatch
 * @param type event type, normally the payload class simple name
 * @param timestamp wall-clock timestamp when the publisher accepted the event, in milliseconds
 * @param workerName thread that published the event
 * @param payload event-specific data
 */
public record EventEnvelope(int eventId, String type, long timestamp, String workerName, MorkEvent payload) {
}
