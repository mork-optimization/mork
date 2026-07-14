package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;
/**
 * Triggered after ending an experiment
 */
public record AlgorithmProcessingEndedEvent(
        String experimentName,
        String instanceName,
        String algorithmName,
        int repetitions
) implements MorkEvent {

    /**
     * Create a new instance processing ended event.
     * The event is triggered by the framework when an instance has been solved by all the algorithms.
     * @param experimentName Current experiment name
     * @param instanceName Instance name
     * @param algorithm algorithm that finished executing
     * @param repetitions
     */
    public AlgorithmProcessingEndedEvent(
            String experimentName,
            String instanceName,
            Algorithm<?, ?> algorithm,
            int repetitions
    ) {
        this(experimentName, instanceName, algorithm.getName(), repetitions);
    }
}
