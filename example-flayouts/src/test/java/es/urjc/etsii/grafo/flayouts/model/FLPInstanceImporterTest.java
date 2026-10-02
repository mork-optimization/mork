package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.flayouts.experiments.SpaceFreeLayoutExperiment;
import es.urjc.etsii.grafo.util.IOUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPInstanceImporterTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void twoRowDatasetLoadsFromArchive() throws Exception {
        int count = 0;
        for (String path : IOUtil.iterate("instances/instances.zip")) {
            String filename = Path.of(IOUtil.entryPath(path)).getFileName().toString();
            if (!filename.endsWith("_2.txt")) {
                continue;
            }
            try (var reader = new BufferedReader(new InputStreamReader(IOUtil.getInputStream(path), StandardCharsets.UTF_8))) {
                var instance = new FLPInstanceImporter().importInstance(reader, filename);
                assertEquals(2, instance.nRows());
                assertEquals(filename.replace(".txt", ""), instance.getId());
                count++;
            }
        }
        assertTrue(count > 0, "The two-row dataset must be available in instances/instances.zip");
    }

    @Test
    void defaultExperimentBuildsAValidSpaceFreeLayoutFromAnImportedInstance() throws Exception {
        try (var reader = new BufferedReader(new StringReader("""
                2
                3
                2 4 6
                0 7 3
                7 0 5
                3 5 0
                """))) {
            var instance = new FLPInstanceImporter().importInstance(reader, "AmExample_2.txt");
            assertEquals("AmExample_2", instance.getId());
            var algorithms = new SpaceFreeLayoutExperiment().getAlgorithms();
            assertFalse(algorithms.isEmpty());
            for (var algorithm : algorithms) {
                FLPNewAlgorithmTest.setBuilder(algorithm);
                assertState(algorithm.algorithm(instance), true);
            }
        }
    }
}
