package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "serializers.testsolution")
public class TestSolutionSerializerConfig extends AbstractSolutionSerializerConfig {
}
