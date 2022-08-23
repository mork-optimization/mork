package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "serializers.test")
public class TestSerializerConfig extends AbstractResultSerializerConfig {
}
