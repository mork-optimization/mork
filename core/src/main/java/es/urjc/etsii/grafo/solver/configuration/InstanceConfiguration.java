package es.urjc.etsii.grafo.solver.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties related to intances
 */
@Configuration
@ConfigurationProperties(prefix = "instances")
public class InstanceConfiguration {
    private Map<String, String> path;

    /**
     * Set instances folder for each experiment
     *
     * @param paths instance paths for each experiment
     */
    public void setPath(Map<String, String> paths) {
        this.path = paths;
    }

    /**
     * Get instances path for a given experiment
     *
     * @param experimentName experiment name
     * @return Instance path as a string
     */
    public String getPath(String experimentName){
        return path.getOrDefault(experimentName, this.path.get("default"));
    }
}
