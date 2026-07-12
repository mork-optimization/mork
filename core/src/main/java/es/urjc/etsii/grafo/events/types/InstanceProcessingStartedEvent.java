package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class InstanceProcessingStartedEvent extends MorkEvent{
    private final String experimentName;
    private final String instanceName;
    private final List<String> algorithms;
    private final int repetitions;
    private final Map<String, Double> refValues;

    /**
     * Create a new InstanceProcessingStartedEvent
     *
     * @param experimentName experiment name
     * @param instanceName instance name
     * @param algorithms list of algorithms to execute in the current experiment
     * @param repetitions number of repetitions for each (instance, algorithm) pair
     * @param refValues best values for the given instance, keyed by objective name
     */
    public InstanceProcessingStartedEvent(String experimentName, String instanceName, List<? extends Algorithm<?, ?>> algorithms, int repetitions, Map<String, Double> refValues) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithms = algorithms.stream().map(Algorithm::getName).collect(Collectors.toList());
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
     * Get reference values for the instance.
     *
     * @return reference values keyed by objective name, or an empty map if none are configured
     */
    public Map<String, Double> getRefValues() {
        return refValues;
    }

    /**
     * Get list of algorithms to execute in the current experiment
     *
     * @return list of algorithms
     */
    public List<String> getAlgorithms() {
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
