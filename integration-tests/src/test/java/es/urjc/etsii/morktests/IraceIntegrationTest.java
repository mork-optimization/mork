package es.urjc.etsii.morktests;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static es.urjc.etsii.morktests.TestUtils.deleteGeneratedFiles;
import static es.urjc.etsii.morktests.TestUtils.runJavaProcess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IraceIntegrationTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void launchAutoconfig(boolean useIndex, @TempDir Path temp) throws Exception {
        Path directory = Path.of("instancesautoconfig/autoconfig").toAbsolutePath();
        Path source = directory;
        var expected = new ArrayList<>(List.of(directory.resolve("fileA.txt").toString(), directory.resolve("fileB.txt").toString()));
        if (useIndex) {
            Path archive = temp.resolve("datasets.zip");
            String entry = "nested/instância two.txt";
            try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
                zip.putNextEntry(new ZipEntry(entry));
                zip.write(Files.readAllBytes(directory.resolve("fileB.txt")));
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("excluded.txt"));
                zip.write("unselected instance".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            source = temp.resolve("training.index");
            Files.writeString(source, "\uFEFF# Training subset\n" + directory.resolve("fileA.txt") + "\ndatasets.zip!" + entry + "\n");
            expected.set(1, archive + "!" + entry);
        }
        Collections.sort(expected);
        int port;
        try (var socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        try {
            int exit = runJavaProcess(Duration.ofMinutes(10),
                    "--autoconfig",
                    "--whitelist=ACITestWhitelist",
                    "--solver.minimum-number-of-experiments=300",
                    "--solver.experiments-per-parameter=10",
                    "--server.port=" + port,
                    "--instances.path.irace=" + source);
            assertEquals(0, exit);
            assertEquals(expected, Files.readAllLines(Path.of("autoconfig-instances.txt"), StandardCharsets.UTF_8));
            assertTrue(Files.exists(Path.of("plots.pdf")));
            assertTrue(Files.exists(Path.of("irace.Rdata")));
            assertTrue(Files.exists(Path.of("log-ablation.Rdata")));
            assertTrue(Files.exists(Path.of("report.html")));
            assertTrue(Files.exists(Path.of("autoconfig-final-elites.json")));

            var verification = new ProcessBuilder("Rscript", "-e", """
                    library(irace)
                    expected <- readLines("autoconfig-instances.txt", encoding = "UTF-8")
                    actual <- getFromNamespace("readInstances", "irace")(
                      instancesDir = "", instancesFile = "autoconfig-instances.txt")
                    stopifnot(identical(actual, expected))
                    load("irace.Rdata")
                    stopifnot(identical(unname(iraceResults$scenario$instances), unname(expected)))
                    """).inheritIO().start();
            try {
                assertTrue(verification.waitFor(30, TimeUnit.SECONDS));
                assertEquals(0, verification.exitValue());
            } finally {
                if (verification.isAlive()) {
                    verification.destroyForcibly();
                }
            }
        } finally {
            deleteGeneratedFiles(
                    Path.of("autoconfig-instances.txt"),
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
}
