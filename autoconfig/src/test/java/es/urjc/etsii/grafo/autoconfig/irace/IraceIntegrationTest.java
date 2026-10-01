package es.urjc.etsii.grafo.autoconfig.irace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static es.urjc.etsii.grafo.util.IOUtil.copyWithSubstitutions;
import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IraceIntegrationTest {

    @Test
    void loadsBundledIraceResourcesFromClasspath() throws IOException {
        assertBundledResourceContains(
                "runner.R",
                "report_progress <- function(iteration, elites, progress, ...)"
        );
        assertBundledResourceContains("runner.R", "progress = progress");
        assertBundledResourceContains("runner.R", "vapply(progress, anyNA");
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

        var elites = IraceFinalElitesUtil.read(sidecar);

        assertEquals(1, elites.size());
        assertEquals("17", elites.getFirst().configurationId());
        assertEquals("VND", elites.getFirst().parameters().get("ROOT"));
    }

    @Test
    void substitutesTrainingManifestInBundledAndCustomScenarios(@TempDir Path temp) throws IOException {
        var substitutions = Map.of(IraceOrchestrator.K_TRAIN_INSTANCES_FILE, "./" + IraceOrchestrator.F_TRAIN_INSTANCES);
        Path bundled = temp.resolve("bundled.txt");
        try (var input = getInputStreamForIrace("scenario.txt", true)) {
            copyWithSubstitutions(input, bundled, substitutions);
        }
        String customTemplate = "trainInstancesDir = \"\"\ntrainInstancesFile = \"__TRAIN_INSTANCES_FILE__\"\nmaxExperiments = 123\n";
        Path custom = temp.resolve("custom.txt");
        try (var input = new ByteArrayInputStream(customTemplate.getBytes(StandardCharsets.UTF_8))) {
            copyWithSubstitutions(input, custom, substitutions);
        }
        for (Path scenario : new Path[]{bundled, custom}) {
            var contents = Files.readString(scenario);
            assertTrue(contents.contains("trainInstancesDir = \"\""));
            assertTrue(contents.contains("trainInstancesFile = \"./autoconfig-instances.txt\""));
            int trainingFileSettings = 0;
            for (String line : Files.readAllLines(scenario)) {
                if (line.startsWith("trainInstancesFile =")) {
                    trainingFileSettings++;
                }
            }
            assertEquals(1, trainingFileSettings);
        }
        assertTrue(Files.readString(custom).contains("maxExperiments = 123"));
    }

    @Test
    void rejectsMissingOrEmptyFinalEliteSidecars(@TempDir Path temp) throws IOException {
        Path sidecar = temp.resolve(IraceIntegration.FINAL_ELITES_FILE);

        assertThrows(IllegalStateException.class, () -> IraceFinalElitesUtil.read(sidecar));

        Files.writeString(sidecar, "{\"elites\":[]}");
        assertThrows(IllegalStateException.class, () -> IraceFinalElitesUtil.read(sidecar));
    }

    private static void assertBundledResourceContains(String filename, String expected) throws IOException {
        try (var resource = getInputStreamForIrace(filename, true)) {
            assertTrue(new String(resource.readAllBytes(), StandardCharsets.UTF_8).contains(expected));
        }
    }
}
