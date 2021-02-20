package es.urjc.etsii.grafo.solver.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "instances")
public class InstanceConfiguration {
    private Map<String, String> path;

    public void setPath(Map<String, String> path) {
        this.path = path;
    }

    public String getPath(String experimentName){
        return path.getOrDefault(experimentName, this.path.get("default"));
    }
}
