package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.util.List;
import java.util.Optional;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class InstanceProcessingStartedEvent extends MorkEvent{
    private final String experimentName;
    private final String instanceName;
    private final List<? extends Algorithm<?,?>> algorithms;
    private final int repetitions;
    private final Optional<Double> referenceValue;

    public InstanceProcessingStartedEvent(String experimentName, String instanceName, List<? extends Algorithm<?, ?>> algorithms, int repetitions, Optional<Double> referenceValue) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithms = algorithms;
        this.repetitions = repetitions;
        this.referenceValue = referenceValue;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Optional<Double> getReferenceValue() {
        return referenceValue;
    }

    public List<? extends Algorithm<?,?>> getAlgorithms() {
        return algorithms;
    }

    public int getRepetitions() {
        return repetitions;
    }
}
