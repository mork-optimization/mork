package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstanceImporter;
import es.urjc.etsii.grafo.exception.InstanceImportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class BMSSCInstanceTest {
    @ParameterizedTest
    @ValueSource(strings = {"", "1,2", "0,1,1", "-1,1,1", "1,0,1", "1,1,0", "1,1,2",
            "a,1,1", "1,1,1\n", "1,2,1\n0", "1,1,1\n0,1", "1,1,1\nNaN",
            "1,1,1\nInfinity", "1,1,1\nxyz", "1,1,1\n0\n1", "1,1,1\n0\n\n",
            "2,1,1\n1e308\n-1e308", "1,1,1\n", "1,1,1\n,"})
    void rejectsMalformedInputWithSourceLocation(String input) {
        var error = assertThrows(InstanceImportException.class, () -> read(input));
        assertTrue(error.getMessage().contains("fixture.csv"));
        assertTrue(error.getMessage().contains("line"));
    }

    @Test
    void supportsWhitespaceDuplicatesSingletonsAndUnequalQuotas() throws Exception {
        var duplicate = read(" 3, 2, 2 \n1,1\n1,1\n1,1\n");
        assertArrayEquals(new int[]{2, 1}, duplicate.getClusterSizes());
        for (String property : new String[]{"distance_min", "distance_max", "distance_avg", "distance_std"}) {
            assertEquals(0.0, duplicate.getProperty(property));
            assertEquals(0.0, read("1,1,1\n4\n").getProperty(property));
        }
        var instance = read("3,1,1\n0\n1\n3\n");
        assertEquals(1.0, instance.getProperty("distance_min"));
        assertEquals(9.0, instance.getProperty("distance_max"));
        assertEquals(14.0 / 3, (double) instance.getProperty("distance_avg"), 1e-12);
    }

    @Test
    void ownsItsCoordinatesAndQuotas() {
        double[][] coordinates = {{0}, {2}, {4}};
        var instance = new BMSSCInstance("immutable", 3, 1, 2, coordinates);
        coordinates[0][0] = 100;
        coordinates[1] = new double[]{500};
        instance.getPoint(0)[0] = 999;
        instance.getClusterSizes()[0] = 999;
        assertEquals(4, instance.distance(0, 1));
        assertArrayEquals(new double[]{0}, instance.getPoint(0));
        assertArrayEquals(new int[]{2, 1}, instance.getClusterSizes());
        assertThrows(IllegalArgumentException.class, () -> new BMSSCInstance("bad", 2, 1, 0, coordinates));
    }

    @Test
    void loadsAnArchiveEntryThroughMork(@TempDir Path directory) throws Exception {
        Path archive = directory.resolve("instances.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("tiny.csv"));
            zip.write("3,1,2\n0\n1\n3\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        var instance = new BMSSCInstanceImporter().importInstance(archive + "!tiny.csv");
        assertEquals(3, instance.n);
        assertEquals(2, instance.k);
        assertEquals(9, instance.distance(0, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"iris.csv", "user_knowledge.csv"})
    void loadsBundledInstancesWithStrictDimensionChecks(String name) {
        var instance = new BMSSCInstanceImporter().importInstance("instances/instances.zip!" + name);
        assertTrue(instance.n > 0);
        assertTrue(Double.isFinite((double) instance.getProperty("distance_avg")));
    }

    private BMSSCInstance read(String input) throws Exception {
        return new BMSSCInstanceImporter().importInstance(new BufferedReader(new StringReader(input)), "fixture.csv");
    }
}
