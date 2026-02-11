package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.orchestrator.InstanceSelector.DEFAULT_OUTPUT_PATH;
import static es.urjc.etsii.morktests.TestUtils.runJavaProcess;

public class ComplexityAnalysisTest {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(ComplexityAnalysisTest.class);
    private static final Path propertiesPath = Path.of(DEFAULT_OUTPUT_PATH);

    @BeforeAll
    static void setup() throws IOException {
        if (Files.exists(propertiesPath)) {
            Files.delete(propertiesPath);
        }
    }


    @Test
    void testComplexity() throws Exception{
        int exit = runJavaProcess(Duration.ofMinutes(5),
                "--server.port=0",
                "--instance-properties",
                "--instances.path.default=instancesautoconfig/sleepy",
                "--event.webserver.stopOnExecutionEnd=true");
        Assertions.assertEquals(0, exit);
        Assertions.assertTrue(Files.exists(propertiesPath));

        log.info("Warming up JVM...");
        runComplexityOnce(); // first one is to allow JVM to warm up, ignore results
        log.info("Starting complexity analysis...");
        exit = runComplexityOnce();

        Assertions.assertEquals(0, exit);
        Assertions.assertTrue(Files.exists(Path.of("solutions")));
        // TODO call python script to analyze results automatically

        // Sleep for 5 seconds to allow the webserver to be stopped
        ConcurrencyUtil.sleep(5, TimeUnit.SECONDS);
    }

    private static int runComplexityOnce() throws IOException, InterruptedException {
        // Execute a normal experiment to extract time statistics from the algorithms we have configured
        FileUtils.deleteDirectory(new File("solutions"));
        return runJavaProcess(Duration.ofMinutes(10),
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
                "--event.webserver.stopOnExecutionEnd=true");
    }

}
