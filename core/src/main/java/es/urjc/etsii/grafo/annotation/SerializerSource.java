package es.urjc.etsii.grafo.annotation;

import es.urjc.etsii.grafo.config.YmlPropSourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@PropertySource(value = "classpath:serializers.yml", factory = YmlPropSourceFactory.class)
@PropertySource(value = "serializers.yml", ignoreResourceNotFound = true, factory = YmlPropSourceFactory.class)
public @interface SerializerSource {}
