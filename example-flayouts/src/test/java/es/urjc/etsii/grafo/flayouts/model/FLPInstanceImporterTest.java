package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.flayouts.experiments.SpaceFreeLayoutExperiment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPInstanceImporterTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void configuredDatasetLoadsFromTheRenamedDirectory() throws Exception {
        int count = 0;
        try (var paths = Files.newDirectoryStream(Path.of("instances", "2rows"), "*.txt")) {
            for (var path : paths) {
                try (var reader = Files.newBufferedReader(path)) {
                    var instance = new FLPInstanceImporter().importInstance(reader, path.getFileName().toString());
                    assertEquals(2, instance.nRows());
                    assertEquals(path.getFileName().toString().replace(".txt", ""), instance.getId());
                    count++;
                }
            }
        }
        assertTrue(count > 0, "The default two-row dataset must be available");
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
