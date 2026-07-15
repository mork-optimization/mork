package es.urjc.etsii.grafo.events.types;

import java.util.List;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public record ExperimentStartedEvent(String experimentName, List<String> instanceNames) implements MorkEvent {

    public ExperimentStartedEvent {
        instanceNames = List.copyOf(instanceNames);
    }
}
