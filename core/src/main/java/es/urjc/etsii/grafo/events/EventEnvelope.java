package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;

/**
 * Public representation of a published event.
 *
 * @param eventId dense event id assigned when the event is accepted by the publisher
 * @param type event type, normally the payload class simple name
 * @param timestamp wall-clock timestamp in milliseconds
 * @param workerName thread that created the original event payload
 * @param payload event-specific data
 */
public record EventEnvelope(int eventId, String type, long timestamp, String workerName, MorkEvent payload) {
}
