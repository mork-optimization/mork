package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;
/**
 * Triggered when starting an experiment before any other action occurs
 */
public record AlgorithmProcessingStartedEvent(
        String experimentName,
        String instanceName,
        String algorithmName,
        int repetitions
) implements MorkEvent {

    /**
     * Create a new AlgorithmProcessingStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceName instance name
     * @param algorithm algorithm that is going to be executed
     * @param repetitions number of repetitions for each (instance, algorithm) pair
     */
    public AlgorithmProcessingStartedEvent(
            String experimentName,
            String instanceName,
            Algorithm<?, ?> algorithm,
            int repetitions
    ) {
        this(experimentName, instanceName, algorithm.getName(), repetitions);
    }
}
