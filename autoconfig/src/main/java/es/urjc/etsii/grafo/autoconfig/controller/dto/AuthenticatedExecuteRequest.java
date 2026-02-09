package es.urjc.etsii.grafo.autoconfig.controller.dto;

public class AuthenticatedExecuteRequest {
    protected final String key;

    public AuthenticatedExecuteRequest(String key) {
        this.key = key;
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
     * Check that the DTO is valid
     */
    public void checkValid(String integrationKey) {
        if(this.key == null || this.key.isBlank()) {
            throw new IllegalArgumentException("Integration key cannot be null");
        }

        if (!getKey().equals(integrationKey)) {
            throw new IllegalArgumentException(String.format("Invalid integration key: %s", getKey()));
        }
    }
}
