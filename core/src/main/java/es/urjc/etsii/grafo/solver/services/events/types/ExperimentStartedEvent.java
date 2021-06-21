package es.urjc.etsii.grafo.solver.services.events.types;

/**
 * Triggered when starting an experiment before any other action occurs
 */
public class ExperimentStartedEvent extends MorkEvent{
    private final String experimentName;

    public ExperimentStartedEvent(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExperimentName() {
        return experimentName;
    }
}
