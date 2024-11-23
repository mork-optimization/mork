package es.urjc.etsii.grafo.annotation;

import es.urjc.etsii.grafo.config.YmlPropSourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Inherited
@PropertySource(value = "classpath:serializers.yml", factory = YmlPropSourceFactory.class)
@PropertySource(value = "file:serializers.yml", ignoreResourceNotFound = true, factory = YmlPropSourceFactory.class)
public @interface SerializerSource {}
