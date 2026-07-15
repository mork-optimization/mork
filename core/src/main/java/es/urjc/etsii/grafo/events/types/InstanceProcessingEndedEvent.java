package es.urjc.etsii.grafo.events.types;

/**
 * Triggered after ending an experiment
 */
public record InstanceProcessingEndedEvent(
        String experimentName,
        String instanceName,
        long executionTime,
        long experimentStartTime
) implements MorkEvent {
}
