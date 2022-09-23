package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.annotation.SerializerSource;
import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SerializerSource
@ConfigurationProperties(prefix = "serializers.test")
public class TestSerializerConfig extends AbstractResultSerializerConfig {
}
