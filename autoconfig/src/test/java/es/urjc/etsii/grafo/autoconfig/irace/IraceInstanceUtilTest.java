package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class IraceInstanceUtilTest {

    @TempDir
    Path temp;

    @ParameterizedTest
    @ValueSource(strings = {"directory", "file", "archive", "index"})
    @SuppressWarnings("unchecked")
    void exportsConfiguredSourcesWithoutImportingInstances(String sourceType) throws IOException {
        Path directory = Files.createDirectory(temp.resolve("training sources"));
        Path regular = directory.resolve("instância one.txt");
        Files.writeString(regular, "regular instance");
        Files.writeString(directory.resolve(".hidden"), "hidden instance");
        Path archive = directory.resolve("datasets.zip");
        String selectedEntry = "nested/archived instance.txt";
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            for (String entry : List.of(selectedEntry, "excluded.txt")) {
                zip.putNextEntry(new ZipEntry(entry));
                zip.write("archived instance".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        Path index = directory.resolve("subset.index");
        Files.writeString(index, "\uFEFF# Training subset\n\ninstância one.txt\ndatasets.zip!" + selectedEntry + "\n");

        Path source;
        List<String> expected;
        String archived = archive + "!" + selectedEntry;
        String excluded = archive + "!excluded.txt";
        switch (sourceType) {
            case "directory" -> {
                source = directory;
                expected = List.of(regular.toString(), archived, excluded);
            }
            case "file" -> {
                source = regular;
                expected = List.of(regular.toString());
            }
            case "archive" -> {
                source = archive;
                expected = List.of(archived, excluded);
            }
            case "index" -> {
                source = index;
                expected = List.of(regular.toString(), archived);
            }
            default -> throw new IllegalArgumentException(sourceType);
        }
        var config = new InstanceConfiguration();
        config.setPreload(true);
        // Exercise the irace override, except for the individual-file case which uses the default.
        config.setPath(sourceType.equals("file")
                ? Map.of("default", source.toString())
                : Map.of("default", "missing", "irace", source.toString()));
        var importer = (InstanceImporter<TestInstance>) mock(InstanceImporter.class);
        var manager = new InstanceManager<>(config, new SolverConfig(), importer);
        Path manifest = temp.resolve("autoconfig-instances.txt");

        IraceInstanceUtil.write(manifest, manager.getInstanceSolveOrder("irace", false));

        var sortedExpected = new ArrayList<>(expected);
        Collections.sort(sortedExpected);
        var actual = Files.readAllLines(manifest, StandardCharsets.UTF_8);
        assertEquals(sortedExpected, actual);
        for (String path : actual) {
            assertEquals(path, manager.requireConfiguredInstancePath("irace", path));
        }
        if (sourceType.equals("index")) {
            assertThrows(IllegalArgumentException.class, () -> manager.requireConfiguredInstancePath("irace", excluded));
        }
        verifyNoInteractions(importer);
        assertEquals("regular instance", Files.readString(regular));
        assertFalse(Files.exists(directory.resolve("nested")));
        try (var files = Files.list(temp)) {
            assertEquals(2, files.count()); // Only the original source directory and the manifest.
        }
    }

    @Test
    void makesRelativeContainersAbsoluteAndPreservesArchiveEntries() throws IOException {
        Path manifest = temp.resolve("instances.txt");
        String regular = "instances/instance one.txt";
        String archive = "instances/data.zip";

        IraceInstanceUtil.write(manifest, List.of(regular, archive + "!nested/instância.txt"));

        assertEquals(List.of(Path.of(regular).toAbsolutePath().toString(),
                Path.of(archive).toAbsolutePath() + "!nested/instância.txt"), Files.readAllLines(manifest));
    }

    @ParameterizedTest
    @ValueSource(strings = {"instance#1.txt", "instance\r1.txt", "data.zip!instance\n1.txt", "data.zip!instance#1.txt"})
    void rejectsPathsThatIraceCannotParse(String path) throws IOException {
        Path manifest = temp.resolve("instances.txt");
        Files.writeString(manifest, "previous manifest\n");

        var error = assertThrows(IllegalArgumentException.class,
                () -> IraceInstanceUtil.write(manifest, List.of("valid.txt", path)));

        assertTrue(error.getMessage().contains("cannot contain '#' or line breaks"));
        assertEquals("previous manifest\n", Files.readString(manifest));
    }

    @Test
    void rejectsEmptySelectionsBeforeWriting() {
        Path manifest = temp.resolve("instances.txt");

        var error = assertThrows(IllegalArgumentException.class, () -> IraceInstanceUtil.write(manifest, List.of()));

        assertTrue(error.getMessage().contains("No training instances"));
        assertFalse(Files.exists(manifest));
    }
}
