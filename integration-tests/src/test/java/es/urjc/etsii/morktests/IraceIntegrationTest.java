package es.urjc.etsii.morktests;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static es.urjc.etsii.morktests.TestUtils.deleteGeneratedFiles;
import static es.urjc.etsii.morktests.TestUtils.runJavaProcess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IraceIntegrationTest {

    @ParameterizedTest(name = "index={0}, dynamic port={1}")
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void launchAutoconfig(boolean useIndex, boolean dynamicPort, @TempDir Path temp) throws Exception {
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
        int port = 0;
        if (!dynamicPort) {
            try (var socket = new ServerSocket(0)) {
                port = socket.getLocalPort();
            }
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

            String scenario = Files.readString(Path.of("scenario.txt"));
            String runner = Files.readString(Path.of("runner.R"));
            var endpoint = Pattern.compile("http://127\\.0\\.0\\.1:(\\d+)/internal/autoconfig/irace/evaluations")
                    .matcher(scenario);
            assertTrue(endpoint.find(), "Scenario must contain the resolved evaluation endpoint");
            int resolvedPort = Integer.parseInt(endpoint.group(1));
            assertTrue(resolvedPort > 0 && resolvedPort <= 65535);
            if (!dynamicPort) assertEquals(port, resolvedPort);
            assertTrue(runner.contains("http://127.0.0.1:" + resolvedPort + "/internal/autoconfig/irace/progress"),
                    "Scenario and runner must use the same bound port");
            for (String generated : List.of(scenario, runner)) {
                assertFalse(generated.contains("__PORT__"), "Port placeholders must be resolved");
                assertFalse(generated.contains("http://127.0.0.1:0/"), "Callbacks cannot use port zero");
            }

            var verification = new ProcessBuilder("Rscript", "-e", """
                    library(irace)
                    expected <- readLines("autoconfig-instances.txt", encoding = "UTF-8")
                    actual <- getFromNamespace("readInstances", "irace")(
                      instancesDir = "", instancesFile = "autoconfig-instances.txt")
                    stopifnot(identical(actual, expected))
                    load("irace.Rdata")
                    stopifnot(identical(unname(iraceResults$scenario$instances), unname(expected)))
                    parameters <- readParameters("parameters.txt")
                    lines <- readLines("parameters.txt")
                    declarations <- lines[seq_len(match("[forbidden]", lines) - 1L)]
                    declarations <- declarations[nzchar(trimws(declarations)) & !startsWith(trimws(declarations), "#")]
                    stopifnot(parameters$nbParameters == length(declarations))
                    stopifnot(length(parameters$forbidden) == 1L)
                    selectors <- paste0("ROOT_SimpleAlgorithm.improver_SequentialImprover.improvers.item", 0:1)
                    stopifnot(all(selectors %in% parameters$names))
                    sampled <- getFromNamespace("sampleUniform", "irace")(parameters, 200L)
                    active <- !is.na(sampled[[selectors[[1L]]]])
                    stopifnot(any(active), any(!active))
                    stopifnot(all(sampled[[selectors[[1L]]]][active] != sampled[[selectors[[2L]]]][active]))
                    invalid <- data.table::copy(sampled[which(active)[[1L]], ])
                    invalid[[selectors[[2L]]]] <- invalid[[selectors[[1L]]]]
                    filtered <- getFromNamespace("filter_forbidden", "irace")(invalid, parameters$forbidden)
                    stopifnot(nrow(filtered) == 0L)
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
