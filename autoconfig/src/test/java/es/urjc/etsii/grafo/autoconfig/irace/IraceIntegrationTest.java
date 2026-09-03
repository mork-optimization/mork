package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.r.RLangRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class IraceIntegrationTest {

    @Test
    void loadsBundledIraceResourcesFromClasspath() throws IOException {
        assertBundledResourceContains("runner.R", "report_progress");
        assertBundledResourceContains("scenario.txt", "targetRunnerParallel");
        assertBundledResourceContains("parameters.txt", "START PARAMETER DECLARATION");
        assertThrows(IOException.class, () -> getInputStreamForIrace("missing", true));
    }

    @Test
    void readsFinalEliteSidecar(@TempDir Path temp) throws IOException {
        Path sidecar = temp.resolve(IraceIntegration.FINAL_ELITES_FILE);
        Files.writeString(sidecar, """
                {
                  "elites": [
                    {
                      "configurationId": "17",
                      "parameters": {
                        "ROOT": "VND",
                        "ROOT_VND.improvers.length": "2"
                      }
                    }
                  ]
                }
                """);

        var integration = new IraceIntegration(mock(RLangRunner.class));
        var elites = integration.readFinalElites(sidecar);

        assertEquals(1, elites.size());
        assertEquals("17", elites.getFirst().configurationId());
        assertEquals("VND", elites.getFirst().parameters().get("ROOT"));
    }

    @Test
    void rejectsMissingOrEmptyFinalEliteSidecars(@TempDir Path temp) throws IOException {
        var integration = new IraceIntegration(mock(RLangRunner.class));
        Path sidecar = temp.resolve(IraceIntegration.FINAL_ELITES_FILE);

        assertThrows(IllegalStateException.class, () -> integration.readFinalElites(sidecar));

        Files.writeString(sidecar, "{\"elites\":[]}");
        assertThrows(IllegalStateException.class, () -> integration.readFinalElites(sidecar));
    }

    private static void assertBundledResourceContains(String filename, String expected) throws IOException {
        try (var resource = getInputStreamForIrace(filename, true)) {
            assertTrue(new String(resource.readAllBytes(), StandardCharsets.UTF_8).contains(expected));
        }
    }
}
