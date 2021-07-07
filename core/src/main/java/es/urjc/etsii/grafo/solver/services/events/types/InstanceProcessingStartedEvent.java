package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.util.List;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class InstanceProcessingStartedEvent extends MorkEvent{
    private final String experimentName;
    private final String instanceName;
    private final List<? extends Algorithm<?,?>> algorithms;
    private final int repetitions;

    public InstanceProcessingStartedEvent(String experimentName, String instanceName, List<? extends Algorithm<?, ?>> algorithms, int repetitions) {
        this.experimentName = experimentName;
        this.instanceName = instanceName;
        this.algorithms = algorithms;
        this.repetitions = repetitions;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public List<? extends Algorithm<?,?>> getAlgorithms() {
        return algorithms;
    }

    public int getRepetitions() {
        return repetitions;
    }
}
