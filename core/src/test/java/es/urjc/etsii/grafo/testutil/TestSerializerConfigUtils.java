package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig;
import es.urjc.etsii.grafo.io.serializers.ResultExportFrequency;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;

import java.nio.file.Path;

public class TestSerializerConfigUtils extends AbstractResultSerializerConfig {
    public static TestSerializerConfig create(boolean enabled, ResultExportFrequency frequency, Path p) {
        return create(enabled, frequency, p, "'Results'_yyyy-MM-dd_HH-mm-ss.'test'");
    }

    public static TestSerializerConfig create(boolean enabled, ResultExportFrequency frequency, Path p, String format) {
        TestSerializerConfig config = new TestSerializerConfig();
        config.setEnabled(enabled);
        config.setFrequency(frequency);
        config.setFolder(p.toFile().getAbsolutePath());
        config.setFormat(format);
        return config;
    }

    public static TestSolutionSerializerConfig createSol(boolean enabled, SolutionExportFrequency frequency, Path p, String format) {
        var config = new TestSolutionSerializerConfig();
        config.setEnabled(enabled);
        config.setFrequency(frequency);
        config.setFolder(p.toFile().getAbsolutePath());
        config.setFormat(format);
        return config;
    }
}
