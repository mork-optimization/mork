//package es.urjc.etsii.grafo.__RNAME__.io;
//
//import es.urjc.etsii.grafo.annotation.SerializerSource;
//import es.urjc.etsii.grafo.io.serializers.AbstractSolutionSerializerConfig;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//
///**
// * Configuration properties for the solution serializer. Properties are loaded from the serializers.yml file, using the
// * node "serializers.solution-custom" by default.
// * First, loads the packaged serializers.yml inside the JAR.
// * Secondly, if there exists a serializers.yml file outside the JAR, override the properties defined in it.
// * <p>
// * Several config properties are inherited, such as "frequency", "enabled", ...
// */
//@SerializerSource
//@ConfigurationProperties(prefix = "serializers.solution-custom")
//public class __RNAME__SolutionExporterConfig extends AbstractSolutionSerializerConfig {
//
//    // This is an example configurable property, remove it if you don't need it, or replace it with your own
//    private boolean enableMyCustomFeature;
//
//
//    // Add Getters and Setters for your custom properties automatically using IntelliJ generator or equivalent
//    public boolean isEnableMyCustomFeature() {
//        return enableMyCustomFeature;
//    }
//
//    public void setEnableMyCustomFeature(boolean enableMyCustomFeature) {
//        this.enableMyCustomFeature = enableMyCustomFeature;
//    }
//}
