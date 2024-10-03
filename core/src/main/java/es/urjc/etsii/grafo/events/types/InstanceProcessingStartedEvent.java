package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.List;
import java.util.Map;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class InstanceProcessingStartedEvent extends MorkEvent{
    private final String experimentName;
    private final String instanceName;
    private final List<? extends Algorithm<?,?>> algorithms;
    private final int repetitions;
    private final Map<String, Double> refValues;

    /**
     * Create a new InstanceProcessingStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceName instance name
     * @param algorithms list of algorithms to execute in the current experiment
     * @param repetitions number of repetitions for each (instance, algorithm) pair
     * @param refValues best value for the given instance if known.
     */
    public InstanceProcessingStartedEvent(String experimentName, String instanceName, List<? extends Algorithm<?, ?>> algorithms, int repetitions, Map<String, Double> refValues) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithms = algorithms;
        this.repetitions = repetitions;
        this.refValues = refValues;
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
     * Get reference value for instance if available
     *
     * @return reference value, or empty if not configured via a ReferenceResultProvider
     */
    public Map<String, Double> getRefValues() {
        return refValues;
    }

    /**
     * Get list of algorithms to execute in the current experiment
     *
     * @return list of algorithms
     */
    public List<? extends Algorithm<?,?>> getAlgorithms() {
        return algorithms;
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
