package es.urjc.etsii.grafo.autoconfig.controller.dto;

import java.util.List;

/**
 * DTO for requesting an execution for a given instance and algorithm.
 * Currently, used for IRACE integration via the ExecutionController.
 */
public class MultiExecuteRequest extends AuthenticatedExecuteRequest{
    private final List<IraceExecuteConfig> experiments;
    private final boolean preflight;

    /**
     * Create a new
     *
     * @param key    integration key, used to validate requests and reject unauthorized ones.
     * @param experiments execution configuration
     * @param preflight whether this batch only validates the IRACE target runner
     */
    public MultiExecuteRequest(String key, List<IraceExecuteConfig> experiments, boolean preflight) {
        super(key);
        this.experiments = experiments;
        this.preflight = preflight;
    }

    /**
     * Get serialized run configuration
     *
     * @return run configuration serialized as string
     */
    public List<IraceExecuteConfig> getExperiments() {
        return experiments;
    }

    public boolean isPreflight() {
        return preflight;
    }

    /**
     * Check that the DTO is valid
     */
    @Override
    public void checkValid(String key) {
        super.checkValid(key);
        if (experiments == null) {
            throw new IllegalArgumentException("Experiments list cannot be null");
        }
        if(experiments.isEmpty()) {
            throw new IllegalArgumentException("Experiments list cannot be empty");
        }
        for (var experiment : experiments) {
            if (experiment == null) {
                throw new IllegalArgumentException("Experiment cannot be null");
            }
            experiment.checkValid();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MultiExecuteRequest{" +
                "experiments='" + experiments + '\'' +
                ", preflight=" + preflight +
                '}';
    }
}
