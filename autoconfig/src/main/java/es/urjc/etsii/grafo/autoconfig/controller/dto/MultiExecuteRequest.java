package es.urjc.etsii.grafo.autoconfig.controller.dto;

import java.util.List;
import java.util.Objects;

/**
 * DTO for requesting an execution for a given instance and algorithm.
 * Currently, used for IRACE integration via the ExecutionController.
 */
public class MultiExecuteRequest extends AuthenticatedExecuteRequest{
    private final List<IraceExecuteConfig> experiments;

    /**
     * Create a new
     *
     * @param key    integration key, used to validate requests and reject unauthorized ones.
     * @param experiments execution configuration
     */
    public MultiExecuteRequest(String key, List<IraceExecuteConfig> experiments) {
        super(key);
        this.experiments = experiments;
    }

    /**
     * Get serialized run configuration
     *
     * @return run configuration serialized as string
     */
    public List<IraceExecuteConfig> getExperiments() {
        return experiments;
    }

    /**
     * Check that the DTO is valid
     */
    @Override
    public void checkValid(String key) {
        super.checkValid(key);
        if(Objects.requireNonNull(experiments).isEmpty()) {
            throw new IllegalArgumentException("Experiments list cannot be empty");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MultiExecuteRequest{" +
                "key='" + key + '\'' +
                ", experiments='" + experiments + '\'' +
                '}';
    }
}
