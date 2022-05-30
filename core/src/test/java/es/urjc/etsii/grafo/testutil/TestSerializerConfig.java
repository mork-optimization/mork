package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;

import java.nio.file.Path;

public class TestSerializerConfig extends AbstractResultSerializerConfig {
    public TestSerializerConfig(boolean enabled, AbstractResultSerializerConfig.Frequency frequency, Path p) {
        this.setEnabled(enabled);
        this.setFrequency(frequency);
        this.setFolder(p.toFile().getAbsolutePath());
        this.setFormat("'Results'_yyyy-MM-dd_HH-mm-ss.'test'");

    }
}
