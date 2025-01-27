package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfigtests.Main;
import es.urjc.etsii.grafo.solver.Mork;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.orchestrator.InstanceSelector.DEFAULT_OUTPUT_PATH;

public class ComplexityAnalysisTest {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(ComplexityAnalysisTest.class);
    private static final Path propertiesPath = Path.of(DEFAULT_OUTPUT_PATH);


    @BeforeAll
    static void setup() throws IOException {
        if (Files.exists(propertiesPath)) {
            Files.delete(propertiesPath);
        }
    }

    static void waitPortClosed(int port) throws InterruptedException{
        // Wait until the webserver is stopped
        long startCheck = System.nanoTime();
        boolean closed = false;
        while (!closed && System.nanoTime() - startCheck < TimeUnit.SECONDS.toNanos(10)) {
            Thread.sleep(1_000);
            try(var socket = new Socket("localhost", port)) {
            } catch (IOException e) {
                closed = true;
            }
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        waitPortClosed(8080);
    }

    @Test
    void testComplexity() throws Exception{
        var success = Mork.start(new String[]{
                "--server.port=0",
                "--instance-properties",
                "--instances.path.default=instancesautoconfig/sleepy",
                "--event.webserver.stopOnExecutionEnd=true",
        }, Main.AC_OBJECTIVE);
        Assertions.assertTrue(success);
        Assertions.assertTrue(Files.exists(propertiesPath));

        log.info("Warming up JVM...");
        runComplexityOnce(); // first one is to allow JVM to warm up, ignore results
        log.info("Starting complexity analysis...");
        success = runComplexityOnce();

        Assertions.assertTrue(success);
        Assertions.assertTrue(Files.exists(Path.of("solutions")));
        // TODO call python script to analyze results automatically

        // Sleep for 5 seconds to allow the webserver to be stopped
        ConcurrencyUtil.sleep(5, TimeUnit.SECONDS);
    }

    private static boolean runComplexityOnce() throws IOException {
        // Execute a normal experiment to extract time statistics from the algorithms we have configured
        FileUtils.deleteDirectory(new File("solutions"));
        return Mork.start(new String[]{
                "--server.port=0",
                "--instances.path.default=instancesautoconfig/sleepy",
                "--solver.iterations=5",
                "--solver.parallelExecutor=true",
                "--solver.nWorkers=30",
                "--solver.metrics=true",
                "--solver.benchmark=false",
                "--serializers.solution-json.enabled=true",
                "--serializers.solution-json.frequency=all",
                "--serializers.solution-json.folder=solutions",
                "--event.webserver.stopOnExecutionEnd=true"
        }, Main.AC_OBJECTIVE);
    }
}
