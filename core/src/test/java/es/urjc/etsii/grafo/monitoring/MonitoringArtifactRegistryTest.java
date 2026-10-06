package es.urjc.etsii.grafo.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class MonitoringArtifactRegistryTest {
    @TempDir Path directory;
    @Test void onlyRegisteredRegularFilesCanBeDownloaded() throws Exception {
        var registry = new MonitoringArtifactRegistry();
        var file = directory.resolve("results.csv"); Files.writeString(file, "data");
        registry.register(file, "experiment", null);
        var id = registry.manifest().getFirst().id();
        assertEquals(file.toRealPath(), registry.available(id)); assertNull(registry.available("../results.csv"));
        assertNull(registry.available(file.toString()));
        Files.delete(file); Files.createSymbolicLink(file, directory.resolve("another-file"));
        assertNull(registry.available(id)); assertTrue(registry.manifest().isEmpty());
    }
}
