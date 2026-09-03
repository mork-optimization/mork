package es.urjc.etsii.morktests;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static es.urjc.etsii.morktests.TestUtils.deleteGeneratedFiles;
import static es.urjc.etsii.morktests.TestUtils.runJavaProcess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IraceIntegrationTest {

    @Test
    void launchAutoconfig() throws Exception {
        int exit = runJavaProcess(Duration.ofMinutes(10),
                "--autoconfig",
                "--whitelist=ACITestWhitelist",
                "--solver.minimum-number-of-experiments=300",
                "--solver.experiments-per-parameter=10",
                "--instances.path.default=instancesautoconfig/autoconfig");
        assertEquals(0, exit);
        assertTrue(Files.exists(Path.of("plots.pdf")));
        assertTrue(Files.exists(Path.of("irace.Rdata")));
        assertTrue(Files.exists(Path.of("log-ablation.Rdata")));
        assertTrue(Files.exists(Path.of("report.html")));
        assertFalse(Files.exists(Path.of("autoconfig-final-elites.json")));

        deleteGeneratedFiles(
                Path.of("autoconfig-final-elites.json"),
                Path.of("irace.Rdata"),
                Path.of("log-ablation.Rdata"),
                Path.of("parameters.txt"),
                Path.of("plots.pdf"),
                Path.of("report.html"),
                Path.of("report_files"),
                Path.of("runner.R"),
                Path.of("runner.R.stderr.log"),
                Path.of("runner.R.stdout.log"),
                Path.of("scenario.txt")
        );
    }
}
