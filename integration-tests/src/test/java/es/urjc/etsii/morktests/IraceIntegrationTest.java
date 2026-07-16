package es.urjc.etsii.morktests;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static es.urjc.etsii.morktests.TestUtils.deleteGeneratedFiles;
import static es.urjc.etsii.morktests.TestUtils.runJavaProcess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IraceIntegrationTest {


    @BeforeAll
    static void setup() throws IOException {
        FileUtils.copyDirectory(new File("../template/src/main/resources/irace"), new File("../integration-tests/src/main/resources/irace"));
        //FileUtils.copyDirectory(new File("../example-tsp/instances"), new File("../integration-tests/instances"));
    }

    @AfterAll
    static void deleteIraceFiles(){
        FileUtils.deleteQuietly(new File("../integration-tests/src/main/resources/irace"));
        //FileUtils.deleteQuietly(new File("../integration-tests/instances"));
        //FileUtils.deleteQuietly(new File("integration-tests/src/main/resources/irace"));
    }

    @Test
    void launchAutoconfig() throws Exception {
        int exit = runJavaProcess(Duration.ofMinutes(10),
                "--autoconfig",
                "--whitelist=ACITestWhitelist",
                "--solver.minimum-number-of-experiments=200",
                "--solver.experiments-per-parameter=10",
                "--instances.path.default=instancesautoconfig/autoconfig");
        assertEquals(0, exit);
        assertTrue(Files.exists(Path.of("plots.pdf")));
        assertTrue(Files.exists(Path.of("irace.Rdata")));
        assertTrue(Files.exists(Path.of("log-ablation.Rdata")));
        assertTrue(Files.exists(Path.of("report.html")));

        deleteGeneratedFiles(
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
