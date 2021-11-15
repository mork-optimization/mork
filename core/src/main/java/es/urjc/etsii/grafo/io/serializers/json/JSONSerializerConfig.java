package es.urjc.etsii.grafo.io.serializers.json;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure the JSON serializer by the properties specified in the application.yml
 * Exports the best solution of each algorithm in JSON format.
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.solution-json")
public class JSONSerializerConfig {

    /**
     * Enable default JSON serializer for solutions
     */
    private boolean enabled = true;

    /**
     * If pretty is true, the json file has multiple indented lines
     */
    private boolean pretty = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}
