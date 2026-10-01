package es.urjc.etsii.grafo.mreflp.io;

import es.urjc.etsii.grafo.annotation.SerializerSource;
import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SerializerSource
@ConfigurationProperties(prefix = "serializers.mreflp")
public class MREFLPResultConfig extends AbstractSolutionSerializerConfig {}
