package es.urjc.etsii.grafo.autoconfig.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AutoconfigArtifactServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void removesKnownArtifactsAndPublishesOnlyFilesFromTheCurrentRun() throws IOException {
        Files.writeString(temporaryDirectory.resolve("plots.pdf"), "stale");
        Files.writeString(temporaryDirectory.resolve("unrelated.txt"), "keep");
        var service = new AutoconfigArtifactService(temporaryDirectory);

        service.prepareRun("run-1");

        assertFalse(Files.exists(temporaryDirectory.resolve("plots.pdf")));
        assertEquals("keep", Files.readString(temporaryDirectory.resolve("unrelated.txt")));
        assertEquals(0, service.manifest("run-1").artifacts().size());
        assertNull(service.manifest("other-run"));

        Files.writeString(temporaryDirectory.resolve("autoconfig-final-elites.json"), "{}");
        Files.writeString(temporaryDirectory.resolve("runner.R.stderr.log"), "failure");

        var manifest = service.manifest("run-1");
        assertEquals(2, manifest.artifacts().size());
        assertEquals("final-elites", manifest.artifacts().getFirst().id());
        assertEquals("application/json", manifest.artifacts().getFirst().mediaType());
        assertEquals("stderr", manifest.artifacts().get(1).id());
    }

    @Test
    void resolvesOnlyWhitelistedArtifactIds() throws IOException {
        var service = new AutoconfigArtifactService(temporaryDirectory);
        service.prepareRun("run-1");
        Files.writeString(temporaryDirectory.resolve("plots.pdf"), "pdf");

        var download = service.download("run-1", "plots");
        assertEquals("plots.pdf", download.filename());
        assertEquals(temporaryDirectory.resolve("plots.pdf"), download.path());
        assertNull(service.download("run-1", "../unrelated"));
        assertNull(service.download("other-run", "plots"));
        assertNull(service.download("run-1", "stdout"));
    }
}
