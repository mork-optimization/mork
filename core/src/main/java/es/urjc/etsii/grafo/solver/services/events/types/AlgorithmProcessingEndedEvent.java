package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

/**
 * Triggered after ending an experiment
 */
public class AlgorithmProcessingEndedEvent<S extends Solution<S, I>, I extends Instance> extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final Algorithm<S,I> algorithm;
    private final int repetitions;

    /**
     * Create a new instance processing ended event.
     * The event is triggered by the framework when an instance has been solved by all the algorithms.
     * @param experimentName Current experiment name
     * @param instanceName Instance name
     * @param algorithm algorithm that finished executing
     * @param repetitions
     */
    public AlgorithmProcessingEndedEvent(String experimentName, String instanceName, Algorithm<S, I> algorithm, int repetitions) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithm = algorithm;
        this.repetitions = repetitions;
    }

    /**
     * Get current experiment name
     *
     * @return experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Get instance name
     *
     * @return instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Get algorithm that finished executing for the current instance
     * @return algorithm reference
     */
    public Algorithm<S, I> getAlgorithm() {
        return algorithm;
    }

    /**
     * How many times is the pair (instance, algorithm) executed?
     * @return
     */
    public int getRepetitions() {
        return repetitions;
    }
}
