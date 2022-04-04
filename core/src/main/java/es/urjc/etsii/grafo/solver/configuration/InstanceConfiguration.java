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

    private boolean preload;

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
    public String getPath(String experimentName) {
        return path.getOrDefault(experimentName, this.path.get("default"));
    }

    /**
     * Loads all instances in RAM before starting each experiment.
     * Can be disabled for example in problems where instances are huge in order to save some RAM.
     *
     * @return is preloading instances enabled?
     */
    public boolean isPreload() {
        return preload;
    }

    /**
     * Loads all instances in RAM before starting each experiment.
     * Can be disabled for example in problems where instances are huge in order to save some RAM.
     * Warning: Disabling it reverts instance solve order to instance name (lexicographically)
     *
     * @param preload change preload configuration to the value given by this parameter
     */
    public void setPreload(boolean preload) {
        this.preload = preload;
    }
}
