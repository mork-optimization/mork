package es.urjc.etsii.grafo.autoconfig.screening;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InstanceScreeningConfig.class)
public class ScreeningConfiguration {}
