package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;

import java.nio.file.Path;

public class TestSerializerConfigUtils extends AbstractResultSerializerConfig {
    public static TestSerializerConfig create(boolean enabled, Frequency frequency, Path p) {
        return create(enabled, frequency, p, "'Results'_yyyy-MM-dd_HH-mm-ss.'test'");
    }

    public static TestSerializerConfig create(boolean enabled, Frequency frequency, Path p, String format) {
        TestSerializerConfig config = new TestSerializerConfig();
        config.setEnabled(enabled);
        config.setFrequency(frequency);
        config.setFolder(p.toFile().getAbsolutePath());
        config.setFormat(format);
        return config;
    }
}
