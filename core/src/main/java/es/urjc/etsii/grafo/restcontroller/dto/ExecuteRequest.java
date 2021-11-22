package es.urjc.etsii.grafo.restcontroller.dto;

import java.util.Objects;

/**
 * DTO for requesting an execution for a given instance and algorithm.
 * Currently used for IRACE integration via the ExecutionController.
 */
public class ExecuteRequest {
    private final String key;
    private final String config;

    /**
     * Create a new
     *
     * @param key integration key, used to validate requests and reject unauthorized ones.
     * @param config execution configuration
     */
    public ExecuteRequest(String key, String config) {
        this.key = key;
        this.config = config;
    }

    /**
     * Get integration key, used to validate requests and reject unauthorized ones.
     *
     * @return integration key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get serialized run configuration
     *
     * @return run configuration serialized as string
     */
    public String getConfig() {
        return config;
    }

    /**
     * Check that the DTO is valid
     *
     * @return boolean if valid, false otherwise
     */
    public boolean isValid(){
        return Objects.nonNull(key)
                && Objects.nonNull(config)
                && !key.isBlank()
                && !config.isBlank();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ExecuteRequest{" +
                "key='" + key + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
