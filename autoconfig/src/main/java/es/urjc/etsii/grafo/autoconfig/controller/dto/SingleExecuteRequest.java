package es.urjc.etsii.grafo.autoconfig.controller.dto;

import java.util.Objects;

/**
 * DTO for requesting an execution for a given instance and algorithm.
 * Currently, used for IRACE integration via the ExecutionController.
 */
public class SingleExecuteRequest extends AuthenticatedExecuteRequest {

    private final IraceExecuteConfig experiment;

    /**
     * Create a new
     *
     * @param key    integration key, used to validate requests and reject unauthorized ones.
     * @param experiment execution configuration
     */
    public SingleExecuteRequest(String key, IraceExecuteConfig experiment) {
        super(key);
        this.experiment = experiment;
    }

    /**
     * Get serialized run configuration
     *
     * @return run configuration serialized as string
     */
    public IraceExecuteConfig getExperiment() {
        return experiment;
    }

    @Override
    public void checkValid(String key) {
        super.checkValid(key);
        Objects.requireNonNull(experiment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SingleExecuteRequest{" +
                "key='" + key + '\'' +
                ", exp='" + experiment + '\'' +
                '}';
    }
}
