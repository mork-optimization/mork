package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.r.RLangRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class IraceIntegrationTest {

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
}
