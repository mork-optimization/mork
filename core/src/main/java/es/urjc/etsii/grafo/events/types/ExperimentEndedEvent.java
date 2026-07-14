package es.urjc.etsii.grafo.events.types;

/**
 * Triggered after ending an experiment
 */
public record ExperimentEndedEvent(
        String experimentName,
        long executionTime,
        long experimentStartTime
) implements MorkEvent {
}
