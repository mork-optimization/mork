package es.urjc.etsii.grafo.flayouts.io;

import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom configuration for FLPSolutionExporter, so it can be configured in the application.yml file
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.solution-flp")
public class FLPSolutionSerializerConfig extends AbstractSolutionSerializerConfig {
}
