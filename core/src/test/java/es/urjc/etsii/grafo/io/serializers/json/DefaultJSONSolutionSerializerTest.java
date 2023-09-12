package es.urjc.etsii.grafo.io.serializers.json;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.serializers.SolutionExportFrequency;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.testutil.TestAssertions;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;

class DefaultJSONSolutionSerializerTest {
    private static final double testScore = 5;

    JSONConfig config;
    Algorithm<TestSolution, TestInstance> algorithm;
    TestSolution solution;
    Path tempDir;

    @BeforeEach
    public void buildConfig(@TempDir Path tempDir) {
        this.config = new JSONConfig();
        config.setEnabled(true);
        config.setFolder(tempDir.toAbsolutePath().toString());
        config.setFormat("'.json'");
        config.setFrequency(SolutionExportFrequency.ALL);
        algorithm = new EmptyAlgorithm<>("testSerializer");
        solution = new TestSolution(new TestInstance("testinstance"), testScore);
        this.tempDir = tempDir;
    }

    @Test
    void exportPretty() throws IOException {
        config.setPretty(true);
        var content = doExport();
        Assertions.assertTrue(content.contains("\n"));
        Assertions.assertTrue(content.contains("  "));
    }

    @Test
    void exportNotPretty() throws IOException{
        config.setPretty(false);
        var content = doExport();
        Assertions.assertFalse(content.contains("\n"));
        Assertions.assertFalse(content.contains("  "));
    }

    private String doExport() throws IOException {
        var serializer = new DefaultJSONSolutionSerializer<TestSolution, TestInstance>(config);
        TestAssertions.toStringImpl(serializer);
        Assertions.assertTrue(serializer.isEnabled());
        var wur = new WorkUnitResult<>("testExperiment", this.algorithm, "bestIteration", this.solution, -1, -1, new MetricsStorage());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> serializer.export(new BufferedWriter(new StringWriter()), wur));
        serializer.exportSolution(wur);
        var paths = Files.list(this.tempDir).toList();
        Assertions.assertEquals(1, paths.size());
        return Files.lines(paths.get(0)).collect(Collectors.joining("\n"));
    }
}