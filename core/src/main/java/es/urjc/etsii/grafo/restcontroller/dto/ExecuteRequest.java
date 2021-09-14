package es.urjc.etsii.grafo.restcontroller.dto;

import java.util.Objects;

public class ExecuteRequest {
    private final String key;
    private final String config;

    public ExecuteRequest(String key, String config) {
        this.key = key;
        this.config = config;
    }

    public String getKey() {
        return key;
    }

    public String getConfig() {
        return config;
    }

    public boolean isValid(){
        return Objects.nonNull(key)
                && Objects.nonNull(config)
                && !key.isBlank()
                && !config.isBlank();
    }

    @Override
    public String toString() {
        return "ExecuteRequest{" +
                "key='" + key + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
