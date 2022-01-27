package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class AlgorithmProcessingStartedEvent<S extends Solution<S, I>, I extends Instance> extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final Algorithm<S,I> algorithm;
    private final int repetitions;

    /**
     * Create a new AlgorithmProcessingStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceName instance name
     * @param algorithm algorithm that is going to be executed
     * @param repetitions number of repetitions for each (instance, algorithm) pair
     */
    public AlgorithmProcessingStartedEvent(String experimentName, String instanceName, Algorithm<S, I> algorithm, int repetitions) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithm = algorithm;
        this.repetitions = repetitions;
    }

    /**
     * Get current experiment name
     *
     * @return current experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Get current instance name
     *
     * @return instance name
     */
    public String getInstanceName() {
        return instanceName;
    }


    /**
     * algorithm that is going to be executed
     *
     * @return list of algorithms
     */
    public Algorithm<S, I> getAlgorithm() {
        return algorithm;
    }

    /**
     * Get number of repetitions for each (instance, algorithm) pair.
     *
     * @return number of repetitions for each (instance, algorithm) pair.
     */
    public int getRepetitions() {
        return repetitions;
    }
}
