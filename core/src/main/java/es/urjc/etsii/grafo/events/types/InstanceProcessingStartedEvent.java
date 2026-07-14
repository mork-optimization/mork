package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public record InstanceProcessingStartedEvent(
        String experimentName,
        String instanceName,
        List<String> algorithms,
        int repetitions,
        Map<String, Double> refValues
) implements MorkEvent {

    public InstanceProcessingStartedEvent {
        algorithms = List.copyOf(algorithms);
        refValues = Collections.unmodifiableMap(new LinkedHashMap<>(refValues));
    }

    /**
     * Create a new InstanceProcessingStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceName instance name
     * @param algorithms list of algorithms to execute in the current experiment
     * @param repetitions number of repetitions for each (instance, algorithm) pair
     * @param refValues best values for the given instance, keyed by objective name
     */
    public InstanceProcessingStartedEvent(
            String experimentName,
            String instanceName,
            Collection<? extends Algorithm<?, ?>> algorithms,
            int repetitions,
            Map<String, Double> refValues
    ) {
        this(
                experimentName,
                instanceName,
                algorithms.stream().map(Algorithm::getName).toList(),
                repetitions,
                refValues
        );
    }
}
